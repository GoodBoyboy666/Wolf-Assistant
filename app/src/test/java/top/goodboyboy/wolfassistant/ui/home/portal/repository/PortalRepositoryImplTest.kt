package top.goodboyboy.wolfassistant.ui.home.portal.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalCacheDataSource
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalRemoteDataSource
import top.goodboyboy.wolfassistant.ui.home.portal.model.CacheDataResult
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalInfoItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.RemoteDataResult

/**
 * PortalRepositoryImpl 的单元测试类
 * 用于验证门户仓库的业务逻辑，包括缓存优先策略、远程数据获取及缓存更新等
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PortalRepositoryImplTest {
    private lateinit var remoteDataSource: PortalRemoteDataSource
    private lateinit var cacheDataSource: PortalCacheDataSource
    private lateinit var repository: PortalRepositoryImpl

    @BeforeEach
    fun setup() {
        remoteDataSource = mockk()
        cacheDataSource = mockk()
        repository = PortalRepositoryImpl(remoteDataSource, cacheDataSource)
    }

    /**
     * 测试 getPortalCategory: 缓存命中
     * 预期：直接返回缓存数据，不调用远程数据源
     */
    @Test
    fun `getPortalCategory returns cached data when cache exists`() =
        runTest {
            // Arrange
            val accessToken = "token"
            val cachedData = listOf(mockk<PortalCategoryItem>())
            coEvery { cacheDataSource.getPortalCategory(any()) } returns CacheDataResult.Success(cachedData)

            // Act
            val result = repository.getPortalCategory(accessToken)

            // Assert
            assertTrue(result is PortalRepository.PortalData.Success)
            assertEquals(cachedData, (result as PortalRepository.PortalData.Success).data)
            coVerify(exactly = 0) { remoteDataSource.getPortalCategory(any()) }
        }

    /**
     * 测试 getPortalCategory: 缓存读取错误
     * 预期：直接返回错误，不调用远程数据源
     */
    @Test
    fun `getPortalCategory returns error when cache fetch fails`() =
        runTest {
            // Arrange
            val accessToken = "token"
            val failure = mockk<Failure>()
            coEvery { cacheDataSource.getPortalCategory(any()) } returns CacheDataResult.Error(failure)

            // Act
            val result = repository.getPortalCategory(accessToken)

            // Assert
            assertTrue(result is PortalRepository.PortalData.Failed)
            assertEquals(failure, (result as PortalRepository.PortalData.Failed).e)
            coVerify(exactly = 0) { remoteDataSource.getPortalCategory(any()) }
        }

    /**
     * 测试 getPortalCategory: 无缓存，远程获取成功
     * 预期：返回远程数据，并将数据保存到缓存
     */
    @Test
    fun `getPortalCategory fetches from remote and caches when no cache`() =
        runTest {
            // Arrange
            val accessToken = "token"
            val remoteData = listOf(mockk<PortalCategoryItem>())

            coEvery { cacheDataSource.getPortalCategory(any()) } returns CacheDataResult.NoCache
            coEvery { remoteDataSource.getPortalCategory(accessToken) } returns RemoteDataResult.Success(remoteData)
            // 修正：返回 SaveResult.Success 而不是 just Runs
            coEvery { cacheDataSource.savePortalCategory(remoteData) } returns PortalCacheDataSource.SaveResult.Success

            // Act
            val result = repository.getPortalCategory(accessToken)

            // Assert
            assertTrue(result is PortalRepository.PortalData.Success)
            assertEquals(remoteData, (result as PortalRepository.PortalData.Success).data)
            coVerify(exactly = 1) { cacheDataSource.savePortalCategory(remoteData) }
        }

    /**
     * 测试 getPortalCategory: 无缓存，远程获取失败
     * 预期：返回错误
     */
    @Test
    fun `getPortalCategory returns error when remote fetch fails`() =
        runTest {
            // Arrange
            val accessToken = "token"
            val failure = mockk<Failure>()

            coEvery { cacheDataSource.getPortalCategory(any()) } returns CacheDataResult.NoCache
            coEvery { remoteDataSource.getPortalCategory(accessToken) } returns RemoteDataResult.Error(failure)

            // Act
            val result = repository.getPortalCategory(accessToken)

            // Assert
            assertTrue(result is PortalRepository.PortalData.Failed)
            assertEquals(failure, (result as PortalRepository.PortalData.Failed).e)
        }

    /**
     * 测试 getPortalInfoList: 缓存命中
     * 预期：直接返回缓存数据，不调用远程数据源
     */
    @Test
    fun `getPortalInfoList returns cached data when cache exists`() =
        runTest {
            // Arrange
            val portalId = "123"
            val cachedData = listOf(mockk<PortalInfoItem>())
            coEvery { cacheDataSource.getPortalInfoList(portalId, any()) } returns CacheDataResult.Success(cachedData)

            // Act
            val result = repository.getPortalInfoList(portalId)

            // Assert
            assertTrue(result is PortalRepository.PortalData.Success)
            assertEquals(cachedData, (result as PortalRepository.PortalData.Success).data)
            coVerify(exactly = 0) { remoteDataSource.getPortalInfoList(any()) }
        }

    /**
     * 测试 getPortalInfoList: 缓存读取错误
     * 预期：返回错误
     */
    @Test
    fun `getPortalInfoList returns error when cache fetch fails`() =
        runTest {
            // Arrange
            val portalId = "123"
            val failure = mockk<Failure>()
            coEvery { cacheDataSource.getPortalInfoList(portalId, any()) } returns CacheDataResult.Error(failure)

            // Act
            val result = repository.getPortalInfoList(portalId)

            // Assert
            assertTrue(result is PortalRepository.PortalData.Failed)
            assertEquals(failure, (result as PortalRepository.PortalData.Failed).e)
        }

    /**
     * 测试 getPortalInfoList: 无缓存，远程获取成功
     * 预期：返回远程数据，并保存到缓存
     */
    @Test
    fun `getPortalInfoList fetches from remote and caches when no cache`() =
        runTest {
            // Arrange
            val portalId = "123"
            val remoteData = listOf(mockk<PortalInfoItem>())

            coEvery { cacheDataSource.getPortalInfoList(portalId, any()) } returns CacheDataResult.NoCache
            coEvery { remoteDataSource.getPortalInfoList(portalId) } returns RemoteDataResult.Success(remoteData)
            // 修正：返回 SaveResult.Success 而不是 just Runs
            coEvery { cacheDataSource.savePortalInfoList(portalId, remoteData) } returns
                PortalCacheDataSource.SaveResult.Success

            // Act
            val result = repository.getPortalInfoList(portalId)

            // Assert
            assertTrue(result is PortalRepository.PortalData.Success)
            assertEquals(remoteData, (result as PortalRepository.PortalData.Success).data)
            coVerify(exactly = 1) { cacheDataSource.savePortalInfoList(portalId, remoteData) }
        }

    /**
     * 测试 getPortalInfoList: 无缓存，远程获取失败
     * 预期：返回错误
     */
    @Test
    fun `getPortalInfoList returns error when remote fetch fails`() =
        runTest {
            // Arrange
            val portalId = "123"
            val failure = mockk<Failure>()

            coEvery { cacheDataSource.getPortalInfoList(portalId, any()) } returns CacheDataResult.NoCache
            coEvery { remoteDataSource.getPortalInfoList(portalId) } returns RemoteDataResult.Error(failure)

            // Act
            val result = repository.getPortalInfoList(portalId)

            // Assert
            assertTrue(result is PortalRepository.PortalData.Failed)
            assertEquals(failure, (result as PortalRepository.PortalData.Failed).e)
        }

    /**
     * 测试 cleanCache
     * 预期：调用 cacheDataSource 的清理方法
     */
    @Test
    fun `cleanCache calls data source clean methods`() =
        runTest {
            // Arrange
            // 修正：返回 CleanResult.Success 而不是 just Runs
            coEvery { cacheDataSource.cleanPortalCategoryCache() } returns PortalCacheDataSource.CleanResult.Success
            coEvery { cacheDataSource.cleanPortalInfoCache() } returns PortalCacheDataSource.CleanResult.Success

            // Act
            repository.cleanCache()

            // Assert
            coVerify {
                cacheDataSource.cleanPortalCategoryCache()
                cacheDataSource.cleanPortalInfoCache()
            }
        }
}
