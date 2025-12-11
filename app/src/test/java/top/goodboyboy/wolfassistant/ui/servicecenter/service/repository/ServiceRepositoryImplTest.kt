package top.goodboyboy.wolfassistant.ui.servicecenter.service.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceCacheDataSource
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceRemoteDataSource
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.ServiceItem
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository.ServiceListData

/**
 * ServiceRepositoryImpl 的单元测试类
 * 验证服务列表仓库的业务逻辑
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ServiceRepositoryImplTest {
    private lateinit var cacheDataSource: ServiceCacheDataSource
    private lateinit var remoteDataSource: ServiceRemoteDataSource
    private lateinit var repository: ServiceRepositoryImpl

    private val testServiceItem =
        ServiceItem(
            imageUrl = "http://example.com/icon.png",
            text = "Test Service",
            serviceUrl = "http://example.com/service",
            tokenAccept = null,
        )

    @BeforeEach
    fun setup() {
        cacheDataSource = mockk()
        remoteDataSource = mockk()
        repository = ServiceRepositoryImpl(cacheDataSource, remoteDataSource)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    /**
     * 测试场景：缓存中存在服务列表
     * 预期：
     * 1. 缓存返回 Success
     * 2. 直接返回缓存数据，不进行网络请求
     */
    @Test
    fun `getServiceList returns cached data when cache exists`() =
        runTest {
            // 准备数据
            val cachedList = listOf(testServiceItem)
            coEvery { cacheDataSource.getServiceList() } returns ServiceCacheDataSource.DataResult.Success(cachedList)

            // 执行操作
            val result = repository.getServiceList("token")

            // 验证结果
            assertTrue(result is ServiceListData.Success)
            assertEquals(cachedList, (result as ServiceListData.Success).data)

            // 验证没有发起网络请求
            coVerify(exactly = 0) { remoteDataSource.getServiceList(any()) }
        }

    /**
     * 测试场景：缓存读取错误
     * 预期：
     * 1. 缓存返回 Error
     * 2. 仓库直接返回 Failed (基于当前实现逻辑)
     */
    @Test
    fun `getServiceList returns failure when cache error occurs`() =
        runTest {
            // 准备数据
            val cacheError = Failure.IOError("Cache Read Error", null)
            coEvery { cacheDataSource.getServiceList() } returns ServiceCacheDataSource.DataResult.Error(cacheError)

            // 执行操作
            val result = repository.getServiceList("token")

            // 验证结果
            assertTrue(result is ServiceListData.Failed)
            assertEquals(cacheError, (result as ServiceListData.Failed).error)

            // 验证没有发起网络请求
            coVerify(exactly = 0) { remoteDataSource.getServiceList(any()) }
        }

    /**
     * 测试场景：缓存不存在（NoCache），网络请求成功
     * 预期：
     * 1. 缓存返回 NoCache
     * 2. 发起网络请求并成功
     * 3. 保存数据到缓存
     * 4. 返回网络数据
     */
    @Test
    fun `getServiceList fetches from remote and caches when cache missing`() =
        runTest {
            // 准备数据
            val remoteList = listOf(testServiceItem)
            coEvery { cacheDataSource.getServiceList() } returns ServiceCacheDataSource.DataResult.NoCache
            coEvery { remoteDataSource.getServiceList("token") } returns
                ServiceRemoteDataSource.DataResult.Success(remoteList)
            coEvery { cacheDataSource.saveServiceList(any()) } returns ServiceCacheDataSource.SaveResult.Success

            // 执行操作
            val result = repository.getServiceList("token")

            // 验证结果
            assertTrue(result is ServiceListData.Success)
            assertEquals(remoteList, (result as ServiceListData.Success).data)

            // 验证保存了缓存
            coVerify(exactly = 1) { cacheDataSource.saveServiceList(remoteList) }
        }

    /**
     * 测试场景：缓存不存在，网络请求失败
     * 预期：
     * 1. 缓存返回 NoCache
     * 2. 网络请求返回 Error
     * 3. 返回 Failed
     */
    @Test
    fun `getServiceList returns failure when cache missing and remote fails`() =
        runTest {
            // 准备数据
            val remoteError = Failure.ApiError(500, "Server Error")
            coEvery { cacheDataSource.getServiceList() } returns ServiceCacheDataSource.DataResult.NoCache
            coEvery { remoteDataSource.getServiceList("token") } returns
                ServiceRemoteDataSource.DataResult.Error(remoteError)

            // 执行操作
            val result = repository.getServiceList("token")

            // 验证结果
            assertTrue(result is ServiceListData.Failed)
            assertEquals(remoteError, (result as ServiceListData.Failed).error)

            // 验证没有尝试保存缓存
            coVerify(exactly = 0) { cacheDataSource.saveServiceList(any()) }
        }

    /**
     * 测试场景：清除缓存
     * 预期：
     * 1. 调用缓存数据源的 cleanServiceList 方法
     */
    @Test
    fun `cleanServiceList delegates to cache data source`() =
        runTest {
            // 准备数据
            coEvery { cacheDataSource.cleanServiceList() } returns ServiceCacheDataSource.CleanResult.Success

            // 执行操作
            repository.cleanServiceList()

            // 验证调用
            coVerify(exactly = 1) { cacheDataSource.cleanServiceList() }
        }
}
