package top.goodboyboy.wolfassistant.ui.schedulecenter

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleCenterRepository
import java.time.LocalDate

/**
 * ScheduleCenterViewModel 的单元测试
 *
 * 测试要点：
 * 1. 验证设置日期功能
 * 2. 验证加载课程表成功场景
 * 3. 验证加载课程表失败场景（日期未设置、仓库返回错误）
 * 4. 验证清理缓存功能
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleCenterViewModelTest {
    private lateinit var viewModel: ScheduleCenterViewModel
    private val scheduleCenterRepository: ScheduleCenterRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val globalEventBus: GlobalEventBus = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock GlobalEventBus events property because subscribeToTarget is inline
        every { globalEventBus.events } returns MutableSharedFlow()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    /**
     * 测试：设置起始和结束日期应更新相应的 StateFlow
     */
    @Test
    fun `setFirstAndLastDay should update firstDay and lastDay flows`() =
        runTest(testDispatcher) {
            viewModel = ScheduleCenterViewModel(scheduleCenterRepository, settingsRepository, globalEventBus)

            val start = LocalDate.of(2023, 1, 1)
            val end = LocalDate.of(2023, 1, 7)

            viewModel.setFirstAndLastDay(start, end)

            assertEquals(start, viewModel.firstDay.value)
            assertEquals(end, viewModel.lastDay.value)
        }

    /**
     * 测试：日期未设置时加载课程表应失败
     */
    @Test
    fun `loadScheduleList should fail when dates are null`() =
        runTest(testDispatcher) {
            viewModel = ScheduleCenterViewModel(scheduleCenterRepository, settingsRepository, globalEventBus)

            // 收集错误信息
            val errorMessages = mutableListOf<String>()
            val job =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.errorMessage.toList(errorMessages)
                }

            viewModel.loadScheduleList()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.loadScheduleState.value is ScheduleCenterViewModel.LoadScheduleState.Failed)
            assertTrue(errorMessages.any { it == "日期不可为Null" })

            job.cancel()
        }

    /**
     * 测试：加载课程表成功
     */
    @Test
    fun `loadScheduleList success should update scheduleList and set Success state`() =
        runTest(testDispatcher) {
            viewModel = ScheduleCenterViewModel(scheduleCenterRepository, settingsRepository, globalEventBus)

            val start = LocalDate.of(2023, 1, 1)
            val end = LocalDate.of(2023, 1, 7)
            val token = "test-token"
            val mockData = listOf(mockk<ScheduleItem>())

            // 设置日期
            viewModel.setFirstAndLastDay(start, end)

            // Mock 依赖
            coEvery { settingsRepository.accessTokenFlow } returns flowOf(token)
            coEvery { scheduleCenterRepository.getSchedule(token, start, end) } returns
                ScheduleCenterRepository.ScheduleData.Success(mockData)

            viewModel.loadScheduleList()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.loadScheduleState.value is ScheduleCenterViewModel.LoadScheduleState.Success)
            assertEquals(mockData, viewModel.scheduleList.value)
        }

    /**
     * 测试：加载课程表失败（仓库返回错误）
     */
    @Test
    fun `loadScheduleList failure from repository should set Failed state and emit error message`() =
        runTest(testDispatcher) {
            viewModel = ScheduleCenterViewModel(scheduleCenterRepository, settingsRepository, globalEventBus)

            val start = LocalDate.of(2023, 1, 1)
            val end = LocalDate.of(2023, 1, 7)
            val token = "test-token"
            val errorMsg = "Network Error"

            // 设置日期
            viewModel.setFirstAndLastDay(start, end)

            // Mock 依赖
            coEvery { settingsRepository.accessTokenFlow } returns flowOf(token)
            coEvery { scheduleCenterRepository.getSchedule(token, start, end) } returns
                ScheduleCenterRepository.ScheduleData.Failed(Failure.IOError(errorMsg, null))

            // 收集错误信息
            val errorMessages = mutableListOf<String>()
            val job =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.errorMessage.toList(errorMessages)
                }

            viewModel.loadScheduleList()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.loadScheduleState.value is ScheduleCenterViewModel.LoadScheduleState.Failed)
            assertTrue(errorMessages.any { it == errorMsg })

            job.cancel()
        }

    /**
     * 测试：清理缓存
     */
    @Test
    fun `cleanCache should call repository cleanScheduleCache`() =
        runTest(testDispatcher) {
            viewModel = ScheduleCenterViewModel(scheduleCenterRepository, settingsRepository, globalEventBus)

            viewModel.cleanCache()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { scheduleCenterRepository.cleanScheduleCache() }
        }
}
