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
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.LabScheduleRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository
import java.time.LocalDate

/**
 * ScheduleCenterViewModel 的单元测试
 *
 * 测试要点：
 * 1. 验证设置日期功能
 * 2. 验证加载课程表成功场景
 * 3. 验证加载课程表失败场景（日期未设置、仓库返回错误）
 * 4. 验证清理缓存功能
 * 5. 验证实验课表加载逻辑
 * 6. 验证周数设置逻辑
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleCenterViewModelTest {
    private lateinit var viewModel: ScheduleCenterViewModel
    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val labScheduleRepository: LabScheduleRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val globalEventBus: GlobalEventBus = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock GlobalEventBus events property because subscribeToTarget is inline
        every { globalEventBus.events } returns MutableSharedFlow()
        // 模拟 selectWeekNum，防止 init 中调用 first() 抛出异常
        every { settingsRepository.selectWeekNum } returns flowOf(1)
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
            viewModel =
                ScheduleCenterViewModel(scheduleRepository, labScheduleRepository, settingsRepository, globalEventBus)

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
            viewModel =
                ScheduleCenterViewModel(scheduleRepository, labScheduleRepository, settingsRepository, globalEventBus)

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
            viewModel =
                ScheduleCenterViewModel(scheduleRepository, labScheduleRepository, settingsRepository, globalEventBus)

            val start = LocalDate.of(2023, 1, 1)
            val end = LocalDate.of(2023, 1, 7)
            val token = "test-token"
            val mockData = listOf(mockk<ScheduleItem>())

            // 设置日期
            viewModel.setFirstAndLastDay(start, end)

            // Mock 依赖
            coEvery { settingsRepository.accessTokenFlow } returns flowOf(token)
            coEvery { scheduleRepository.getSchedule(token, start, end) } returns
                ScheduleRepository.ScheduleData.Success(mockData)

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
            viewModel =
                ScheduleCenterViewModel(scheduleRepository, labScheduleRepository, settingsRepository, globalEventBus)

            val start = LocalDate.of(2023, 1, 1)
            val end = LocalDate.of(2023, 1, 7)
            val token = "test-token"
            val errorMsg = "Network Error"

            // 设置日期
            viewModel.setFirstAndLastDay(start, end)

            // Mock 依赖
            coEvery { settingsRepository.accessTokenFlow } returns flowOf(token)
            coEvery { scheduleRepository.getSchedule(token, start, end) } returns
                ScheduleRepository.ScheduleData.Failed(Failure.IOError(errorMsg, null))

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
     * 测试：加载实验课表成功
     */
    @Test
    fun `loadLabScheduleList success should update labScheduleList and set Success state`() =
        runTest(testDispatcher) {
            viewModel =
                ScheduleCenterViewModel(scheduleRepository, labScheduleRepository, settingsRepository, globalEventBus)

            val week = 1
            val mockData = listOf(mockk<LabScheduleItem>())

            // Mock依赖
            coEvery { labScheduleRepository.getLabSchedule(week) } returns
                LabScheduleRepository.LabScheduleData.Success(mockData)

            // 确保初始周数为1
            assertEquals(1, viewModel.weekNumber.value)

            viewModel.loadLabScheduleList()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.loadLabScheduleState.value is ScheduleCenterViewModel.LoadScheduleState.Success)
            assertEquals(mockData, viewModel.labScheduleList.value)
        }

    /**
     * 测试：加载实验课表失败
     */
    @Test
    fun `loadLabScheduleList failure should set Failed state and emit error message`() =
        runTest(testDispatcher) {
            viewModel =
                ScheduleCenterViewModel(scheduleRepository, labScheduleRepository, settingsRepository, globalEventBus)

            val week = 1
            val errorMsg = "Lab Error"

            // Mock依赖
            coEvery { labScheduleRepository.getLabSchedule(week) } returns
                LabScheduleRepository.LabScheduleData.Failed(Failure.IOError(errorMsg, null))

            // 收集错误信息
            val errorMessages = mutableListOf<String>()
            val job =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.errorMessage.toList(errorMessages)
                }

            viewModel.loadLabScheduleList()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.loadLabScheduleState.value is ScheduleCenterViewModel.LoadScheduleState.Failed)
            // 错误信息拼接了 cause?.message，这里 cause 为 null，所以只包含 errorMsg + "null"
            assertTrue(errorMessages.any { it.contains(errorMsg) })

            job.cancel()
        }

    /**
     * 测试：设置当前周数应更新 weekNumber 并保存到 SettingsRepository
     */
    @Test
    fun `setSelectedWeek should update weekNumber and save to settings`() =
        runTest(testDispatcher) {
            viewModel =
                ScheduleCenterViewModel(scheduleRepository, labScheduleRepository, settingsRepository, globalEventBus)
            // 确保 init 块中的协程先执行完毕
            testDispatcher.scheduler.advanceUntilIdle()

            val newWeek = 5
            viewModel.setSelectedWeek(newWeek)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(newWeek, viewModel.weekNumber.value)
            coVerify(exactly = 1) { settingsRepository.setSelectWeekNum(newWeek) }
        }

    /**
     * 测试：初始化时应从 SettingsRepository 加载周数
     */
    @Test
    fun `init should load weekNumber from settings`() =
        runTest(testDispatcher) {
            val savedWeek = 3
            every { settingsRepository.selectWeekNum } returns flowOf(savedWeek)

            viewModel =
                ScheduleCenterViewModel(scheduleRepository, labScheduleRepository, settingsRepository, globalEventBus)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(savedWeek, viewModel.weekNumber.value)
        }

    /**
     * 测试：清理缓存
     */
    @Test
    fun `cleanCache should call repository cleanScheduleCache`() =
        runTest(testDispatcher) {
            viewModel =
                ScheduleCenterViewModel(scheduleRepository, labScheduleRepository, settingsRepository, globalEventBus)

            viewModel.cleanCache()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { scheduleRepository.cleanScheduleCache() }
        }

    /**
     * 测试：清理实验课表缓存
     */
    @Test
    fun `cleanLabCache should call repository cleanLabScheduleCache`() =
        runTest(testDispatcher) {
            viewModel =
                ScheduleCenterViewModel(scheduleRepository, labScheduleRepository, settingsRepository, globalEventBus)

            viewModel.cleanLabCache()
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { labScheduleRepository.cleanLabScheduleCache() }
        }
}
