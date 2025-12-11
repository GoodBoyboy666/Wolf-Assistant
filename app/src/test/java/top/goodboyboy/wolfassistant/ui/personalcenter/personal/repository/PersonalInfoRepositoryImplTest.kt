package top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository

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
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoRemoteDataSource
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.model.PersonalInfo
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository.PersonalInfoData

/**
 * PersonalInfoRepositoryImpl 的单元测试类
 * 用于验证个人信息仓库的业务逻辑，包括缓存优先策略和网络请求回退机制。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PersonalInfoRepositoryImplTest {
    private lateinit var cacheDataSource: PersonalInfoCacheDataSource
    private lateinit var remoteDataSource: PersonalInfoRemoteDataSource
    private lateinit var repository: PersonalInfoRepositoryImpl

    private val testPersonalInfo =
        PersonalInfo(
            userUid = "123",
            userName = "Test User",
            organizationName = "Test Org",
            identityTypeName = "Student",
            imageUrl = "http://example.com/avatar.png",
        )

    @BeforeEach
    fun setup() {
        cacheDataSource = mockk()
        remoteDataSource = mockk()
        repository = PersonalInfoRepositoryImpl(cacheDataSource, remoteDataSource)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    /**
     * 测试场景：缓存中存在个人信息
     * 预期：
     * 1. 从缓存数据源获取数据成功
     * 2. 直接返回缓存中的数据，不进行网络请求
     */
    @Test
    fun `getPersonalInfo returns cached data when cache exists`() =
        runTest {
            // 准备数据
            coEvery { cacheDataSource.getPersonalInfo() } returns
                PersonalInfoCacheDataSource.DataResult.Success(testPersonalInfo)

            // 执行操作
            val result = repository.getPersonalInfo("token")

            // 验证结果
            assertTrue(result is PersonalInfoData.Success)
            assertEquals(testPersonalInfo, (result as PersonalInfoData.Success).data)

            // 验证没有发起网络请求
            coVerify(exactly = 0) { remoteDataSource.getPersonalInfo(any()) }
        }

    /**
     * 测试场景：缓存不存在（NoCache），网络请求成功
     * 预期：
     * 1. 缓存返回 NoCache
     * 2. 发起网络请求并成功获取数据
     * 3. 将网络获取的数据保存到缓存
     * 4. 返回网络获取的数据
     */
    @Test
    fun `getPersonalInfo fetches from remote and caches when cache missing`() =
        runTest {
            // 准备数据
            coEvery { cacheDataSource.getPersonalInfo() } returns PersonalInfoCacheDataSource.DataResult.NoCache
            coEvery { remoteDataSource.getPersonalInfo("token") } returns
                PersonalInfoRemoteDataSource.DataResult.Success(testPersonalInfo)
            coEvery { cacheDataSource.savePersonalInfo(testPersonalInfo) } returns
                PersonalInfoCacheDataSource.SaveResult.Success

            // 执行操作
            val result = repository.getPersonalInfo("token")

            // 验证结果
            assertTrue(result is PersonalInfoData.Success)
            assertEquals(testPersonalInfo, (result as PersonalInfoData.Success).data)

            // 验证保存了缓存
            coVerify(exactly = 1) { cacheDataSource.savePersonalInfo(testPersonalInfo) }
        }

    /**
     * 测试场景：缓存读取出错，网络请求成功
     * 预期：
     * 1. 缓存返回 Error
     * 2. 忽略缓存错误，发起网络请求并成功
     * 3. 将数据保存到缓存
     * 4. 返回网络获取的数据
     */
    @Test
    fun `getPersonalInfo fetches from remote when cache error`() =
        runTest {
            // 准备数据
            coEvery { cacheDataSource.getPersonalInfo() } returns
                PersonalInfoCacheDataSource.DataResult.Error(Failure.IOError("Test IO Error", null))
            coEvery { remoteDataSource.getPersonalInfo("token") } returns
                PersonalInfoRemoteDataSource.DataResult.Success(testPersonalInfo)
            coEvery { cacheDataSource.savePersonalInfo(testPersonalInfo) } returns
                PersonalInfoCacheDataSource.SaveResult.Success

            // 执行操作
            val result = repository.getPersonalInfo("token")

            // 验证结果
            assertTrue(result is PersonalInfoData.Success)
            assertEquals(testPersonalInfo, (result as PersonalInfoData.Success).data)

            // 验证保存了缓存
            coVerify(exactly = 1) { cacheDataSource.savePersonalInfo(testPersonalInfo) }
        }

    /**
     * 测试场景：缓存不存在，网络请求失败
     * 预期：
     * 1. 缓存返回 NoCache
     * 2. 网络请求返回 Error
     * 3. 最终返回 Failed 状态
     */
    @Test
    fun `getPersonalInfo returns error when both cache and remote fail`() =
        runTest {
            // 准备数据
            val remoteError = Failure.ApiError(500, "Server Error")
            coEvery { cacheDataSource.getPersonalInfo() } returns PersonalInfoCacheDataSource.DataResult.NoCache
            coEvery { remoteDataSource.getPersonalInfo("token") } returns
                PersonalInfoRemoteDataSource.DataResult.Error(remoteError)

            // 执行操作
            val result = repository.getPersonalInfo("token")

            // 验证结果
            assertTrue(result is PersonalInfoData.Failed)
            assertEquals(remoteError, (result as PersonalInfoData.Failed).error)

            // 验证没有尝试保存缓存
            coVerify(exactly = 0) { cacheDataSource.savePersonalInfo(any()) }
        }

    /**
     * 测试场景：清除缓存
     * 预期：
     * 1. 调用缓存数据源的 cleanPersonalInfo 方法
     */
    @Test
    fun `cleanPersonalInfoCache delegates to cache data source`() =
        runTest {
            // 准备数据
            coEvery { cacheDataSource.cleanPersonalInfo() } returns PersonalInfoCacheDataSource.CleanResult.Success

            // 执行操作
            repository.cleanPersonalInfoCache()

            // 验证调用
            coVerify(exactly = 1) { cacheDataSource.cleanPersonalInfo() }
        }
}
