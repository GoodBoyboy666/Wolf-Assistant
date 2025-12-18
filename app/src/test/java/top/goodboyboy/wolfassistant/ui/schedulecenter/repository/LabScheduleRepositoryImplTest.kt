package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.LabScheduleCacheDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.LabScheduleRemoteDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem

/**
 * LabScheduleRepositoryImpl 的单元测试
 * 验证实验室课表数据的获取逻辑，包括缓存优先策略和远程数据获取
 */
class LabScheduleRepositoryImplTest {
    private lateinit var repository: LabScheduleRepositoryImpl
    private val remoteDataSource: LabScheduleRemoteDataSource = mockk()
    private val cacheDataSource: LabScheduleCacheDataSource = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk()

    @BeforeEach
    fun setup() {
        repository = LabScheduleRepositoryImpl(remoteDataSource, cacheDataSource, settingsRepository)
        // 模拟 SettingsRepository 返回的用户信息
        coEvery { settingsRepository.userIDFlow } returns flowOf("testUser")
        coEvery { settingsRepository.userPasswdFlow } returns flowOf("testPass")
    }

    /**
     * 测试：当缓存存在且有效时，应直接返回缓存数据，不请求远程数据
     */
    @Test
    fun `getLabSchedule returns cached data when cache exists`() =
        runTest {
            val week = 1
            val cachedData = listOf(LabScheduleItem("Course1", "Code1", "Class1", "Room1", "1-2"))
            coEvery { cacheDataSource.getLabScheduleCache(week) } returns
                LabScheduleCacheDataSource.LabScheduleResult.Success(cachedData)

            val result = repository.getLabSchedule(week)

            assertTrue(result is LabScheduleRepository.LabScheduleData.Success)
            assertEquals(cachedData, (result as LabScheduleRepository.LabScheduleData.Success).data)
            // 验证没有调用远程数据源
            coVerify(exactly = 0) { remoteDataSource.getLabSchedule(any(), any()) }
        }

    /**
     * 测试：当缓存读取失败（报错）时，应直接返回失败
     */
    @Test
    fun `getLabSchedule returns failure when cache error occurs`() =
        runTest {
            val week = 1
            val error = Failure.IOError("Cache Error", null)
            coEvery { cacheDataSource.getLabScheduleCache(week) } returns
                LabScheduleCacheDataSource.LabScheduleResult.Error(error)

            val result = repository.getLabSchedule(week)

            assertTrue(result is LabScheduleRepository.LabScheduleData.Failed)
            assertEquals(error, (result as LabScheduleRepository.LabScheduleData.Failed).error)
        }

    /**
     * 测试：当无缓存时，应请求远程数据，成功后保存缓存并返回数据
     */
    @Test
    fun `getLabSchedule fetches from remote and saves cache when no cache exists`() =
        runTest {
            val week = 1
            val remoteDataMap =
                mapOf(
                    1 to listOf(LabScheduleItem("CourseRemote", "CodeRemote", "ClassRemote", "RoomRemote", "3-4")),
                )

            coEvery {
                cacheDataSource.getLabScheduleCache(
                    week,
                )
            } returns LabScheduleCacheDataSource.LabScheduleResult.NoCache
            coEvery { remoteDataSource.getLabSchedule("testUser", "testPass") } returns
                LabScheduleRemoteDataSource.LabScheduleDataResult.Success(remoteDataMap)

            val result = repository.getLabSchedule(week)

            assertTrue(result is LabScheduleRepository.LabScheduleData.Success)
            assertEquals(remoteDataMap[1], (result as LabScheduleRepository.LabScheduleData.Success).data)

            // 验证保存了缓存
            coVerify(exactly = 1) { cacheDataSource.saveLabScheduleCache(remoteDataMap) }
        }

    /**
     * 测试：当无缓存且远程请求失败时，应返回失败
     */
    @Test
    fun `getLabSchedule returns failure when no cache and remote fails`() =
        runTest {
            val week = 1
            val error = Failure.IOError("Network Error", null)

            coEvery {
                cacheDataSource.getLabScheduleCache(
                    week,
                )
            } returns LabScheduleCacheDataSource.LabScheduleResult.NoCache
            coEvery { remoteDataSource.getLabSchedule("testUser", "testPass") } returns
                LabScheduleRemoteDataSource.LabScheduleDataResult.Error(error)

            val result = repository.getLabSchedule(week)

            assertTrue(result is LabScheduleRepository.LabScheduleData.Failed)
            assertEquals(error, (result as LabScheduleRepository.LabScheduleData.Failed).error)
        }

    /**
     * 测试：清理缓存功能应调用 DataSource 的清理方法
     */
    @Test
    fun `cleanLabScheduleCache calls datasource clean method`() =
        runTest {
            repository.cleanLabScheduleCache()
            coVerify(exactly = 1) { cacheDataSource.cleanLabScheduleCache() }
        }
}
