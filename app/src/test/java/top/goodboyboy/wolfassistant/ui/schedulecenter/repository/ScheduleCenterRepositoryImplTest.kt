package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleCacheDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleCenterRepository.ScheduleData
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * ScheduleCenterRepositoryImpl 的单元测试类
 * 验证课表仓库的缓存策略和数据获取逻辑
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleCenterRepositoryImplTest {
    private lateinit var cacheDataSource: ScheduleCacheDataSource
    private lateinit var remoteDataSource: ScheduleRemoteDataSource
    private lateinit var repository: ScheduleCenterRepositoryImpl

    private val testDate = LocalDate.of(2023, 10, 1)
    private val testScheduleItem =
        ScheduleItem(
            title = "Math",
            startDate = OffsetDateTime.of(testDate, LocalTime.of(8, 0), ZoneOffset.UTC),
            startTime = LocalTime.of(8, 0),
            endDate = OffsetDateTime.of(testDate, LocalTime.of(10, 0), ZoneOffset.UTC),
            endTime = LocalTime.of(10, 0),
            address = "Room 101",
            remark = "Calculus",
            startDateStr = "2023-10-01",
            endDateStr = "2023-10-01",
        )

    @BeforeEach
    fun setup() {
        cacheDataSource = mockk()
        remoteDataSource = mockk()
        repository = ScheduleCenterRepositoryImpl(cacheDataSource, remoteDataSource)

        // Mock android.util.Log 因为实现类中使用了它
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    /**
     * 测试场景：缓存中存在课表数据
     * 预期：
     * 1. 缓存返回 Success
     * 2. 直接返回缓存数据，不进行网络请求
     */
    @Test
    fun `getSchedule returns cached data when cache exists`() =
        runTest {
            // 准备数据
            val cachedList = listOf(testScheduleItem)
            coEvery { cacheDataSource.getSchedule(any(), any()) } returns
                ScheduleCacheDataSource.DataResult.Success(cachedList)

            // 执行操作
            val result = repository.getSchedule("token", testDate, testDate)

            // 验证结果
            assertTrue(result is ScheduleData.Success)
            assertEquals(cachedList, (result as ScheduleData.Success).data)

            // 验证没有发起网络请求
            coVerify(exactly = 0) { remoteDataSource.getSchedule(any(), any(), any()) }
        }

    /**
     * 测试场景：缓存读取错误
     * 预期：
     * 1. 缓存返回 Error
     * 2. 仓库直接返回 Failed，不再尝试网络请求 (基于当前实现)
     */
    @Test
    fun `getSchedule returns failure when cache error occurs`() =
        runTest {
            // 准备数据
            val cacheError = Failure.IOError("Cache Read Error", null)
            coEvery { cacheDataSource.getSchedule(any(), any()) } returns
                ScheduleCacheDataSource.DataResult.Error(cacheError)

            // 执行操作
            val result = repository.getSchedule("token", testDate, testDate)

            // 验证结果
            assertTrue(result is ScheduleData.Failed)
            assertEquals(cacheError, (result as ScheduleData.Failed).error)

            // 验证没有发起网络请求
            coVerify(exactly = 0) { remoteDataSource.getSchedule(any(), any(), any()) }
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
    fun `getSchedule fetches from remote and caches when cache missing`() =
        runTest {
            // 准备数据
            val remoteList = listOf(testScheduleItem)
            coEvery { cacheDataSource.getSchedule(any(), any()) } returns ScheduleCacheDataSource.DataResult.NoCache
            coEvery { remoteDataSource.getSchedule(any(), any(), any()) } returns
                ScheduleRemoteDataSource.DataResult.Success(remoteList)
            coEvery { cacheDataSource.saveSchedule(any(), any(), any()) } returns
                ScheduleCacheDataSource.SaveResult.Success

            // 执行操作
            val result = repository.getSchedule("token", testDate, testDate)

            // 验证结果
            assertTrue(result is ScheduleData.Success)
            assertEquals(remoteList, (result as ScheduleData.Success).data)

            // 验证保存了缓存
            coVerify(exactly = 1) { cacheDataSource.saveSchedule(testDate, testDate, remoteList) }
        }

    /**
     * 测试场景：缓存不存在，网络请求失败
     * 预期：
     * 1. 缓存返回 NoCache
     * 2. 网络请求返回 Error
     * 3. 返回 Failed
     */
    @Test
    fun `getSchedule returns failure when cache missing and remote fails`() =
        runTest {
            // 准备数据
            val remoteError = Failure.ApiError(404, "Not Found")
            coEvery { cacheDataSource.getSchedule(any(), any()) } returns ScheduleCacheDataSource.DataResult.NoCache
            coEvery { remoteDataSource.getSchedule(any(), any(), any()) } returns
                ScheduleRemoteDataSource.DataResult.Error(remoteError)

            // 执行操作
            val result = repository.getSchedule("token", testDate, testDate)

            // 验证结果
            assertTrue(result is ScheduleData.Failed)
            assertEquals(remoteError, (result as ScheduleData.Failed).error)

            // 验证没有保存缓存
            coVerify(exactly = 0) { cacheDataSource.saveSchedule(any(), any(), any()) }
        }

    /**
     * 测试场景：清除缓存
     * 预期：
     * 1. 调用缓存数据源的 cleanSchedule 方法
     */
    @Test
    fun `cleanScheduleCache delegates to cache data source`() =
        runTest {
            // 准备数据
            coEvery { cacheDataSource.cleanSchedule() } returns ScheduleCacheDataSource.CleanResult.Success

            // 执行操作
            repository.cleanScheduleCache()

            // 验证调用
            coVerify(exactly = 1) { cacheDataSource.cleanSchedule() }
        }
}
