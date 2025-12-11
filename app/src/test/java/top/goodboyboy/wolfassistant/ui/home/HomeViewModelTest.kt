package top.goodboyboy.wolfassistant.ui.home

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalInfoItem
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository
import java.time.LocalTime

/**
 * HomeViewModel 的单元测试
 *
 * 测试要点：
 *  - loadTimeTalk 根据时间设置问候语
 *  - 初始化时会从 PortalRepository 加载门户分类和信息，并根据结果更新状态
 *  - cleanPortal 会清理内存数据并调用仓库的 cleanCache
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var viewModel: HomeViewModel
    private val portalRepository: PortalRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    /**
     * 在每个测试前设置主协程调度器为测试调度器
     * 以及为 SettingsRepository 提供默认的 Flow 返回值，避免真实 DataStore 访问
     */
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // default settings repository flows
        every { settingsRepository.userNameFlow } returns flowOf("TestUser")
        every { settingsRepository.accessTokenFlow } returns flowOf("TestToken")
    }

    /**
     * 在每个测试后重置主协程调度器并清理 MockK 状态
     */
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    /**
     * 测试：在早晨时段调用 loadTimeTalk 会设置为早安问候语
     */
    @Test
    fun `loadTimeTalk sets morning greeting`() =
        runTest(testDispatcher) {
            mockkStatic(LocalTime::class)
            every { LocalTime.now() } returns LocalTime.of(8, 0)

            viewModel = HomeViewModel(portalRepository, settingsRepository)

            assertEquals("新的一天又开始了，祝你过得快乐!", viewModel.timeTalk.value)
        }

    /**
     * 测试：初始化时成功加载门户分类与信息，并将 portalState 置为 Success
     */
    @Test
    fun `init loads portal categories and infos successfully`() =
        runTest(testDispatcher) {
            val categories = listOf(PortalCategoryItem("1", "Category 1"))
            val infos = listOf(PortalInfoItem("Title", "Author", "2025-01-01", "https://example.com"))

            coEvery { portalRepository.getPortalCategory("TestToken") } returns
                PortalRepository.PortalData.Success(categories)
            coEvery { portalRepository.getPortalInfoList("1") } returns PortalRepository.PortalData.Success(infos)
            mockkStatic(LocalTime::class)
            every { LocalTime.now() } returns LocalTime.of(10, 0)

            viewModel = HomeViewModel(portalRepository, settingsRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(categories, viewModel.portalCategoryList.value)
            assertEquals(listOf(infos), viewModel.portalInfoList.value)
            assertTrue(viewModel.portalState.value is HomeViewModel.PortalState.Success)
        }

    /**
     * 测试：当门户分类加载失败时，portalState 应为 Failed，且数据列表保持为空
     */
    @Test
    fun `init handles portal category loading failure`() =
        runTest(testDispatcher) {
            val failure = Failure.IOError("Network error", null)
            coEvery { portalRepository.getPortalCategory("TestToken") } returns
                PortalRepository.PortalData.Failed(failure)
            mockkStatic(LocalTime::class)
            every { LocalTime.now() } returns LocalTime.of(10, 0)

            viewModel = HomeViewModel(portalRepository, settingsRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.portalState.value
            assertTrue(state is HomeViewModel.PortalState.Failed)
            val msg = (state as HomeViewModel.PortalState.Failed).message
            assertEquals("Network error", msg)

            // lists should remain empty on failure
            assertEquals(emptyList<PortalCategoryItem>(), viewModel.portalCategoryList.value)
            assertEquals(emptyList<List<PortalInfoItem>>(), viewModel.portalInfoList.value)
        }

    /**
     * 测试：cleanPortal 会清空内存中门户分类和信息，并调用仓库的清理方法
     */
    @Test
    fun `cleanPortal clears data and calls repository clean`() =
        runTest(testDispatcher) {
            val categories = listOf(PortalCategoryItem("1", "Category 1"))
            val infos = listOf(PortalInfoItem("Title", "Author", "2025-01-01", "https://example.com"))

            coEvery { portalRepository.getPortalCategory("TestToken") } returns
                PortalRepository.PortalData.Success(categories)
            coEvery { portalRepository.getPortalInfoList("1") } returns PortalRepository.PortalData.Success(infos)
            coEvery { portalRepository.cleanCache() } returns Unit
            mockkStatic(LocalTime::class)
            every { LocalTime.now() } returns LocalTime.of(10, 0)

            viewModel = HomeViewModel(portalRepository, settingsRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            // ensure data loaded
            assertEquals(categories, viewModel.portalCategoryList.value)
            assertEquals(listOf(infos), viewModel.portalInfoList.value)

            // call clean
            viewModel.cleanPortal()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(emptyList<PortalCategoryItem>(), viewModel.portalCategoryList.value)
            assertEquals(emptyList<List<PortalInfoItem>>(), viewModel.portalInfoList.value)
            coVerify(exactly = 1) { portalRepository.cleanCache() }
        }
}
