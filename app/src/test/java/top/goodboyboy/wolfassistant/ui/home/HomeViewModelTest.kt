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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
        coEvery { settingsRepository.getAccessTokenDecrypted() } returns "TestToken"
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
     * 测试：loadTimeTalk 根据小时返回正确的问候语（边界测试）
     */
    @ParameterizedTest
    @CsvSource(
        "0, 现在已经过凌晨了，身体是无价的资本喔，早点休息吧！",
        "1, 现在已经过凌晨了，身体是无价的资本喔，早点休息吧！",
        "2, 该休息了，身体可是革命的本钱啊！",
        "3, 该休息了，身体可是革命的本钱啊！",
        "4, 该休息了，身体可是革命的本钱啊！",
        "5, 快要熬穿啦，赶紧去补补觉吧！",
        "6, 快要熬穿啦，赶紧去补补觉吧！",
        "7, 新的一天又开始了，祝你过得快乐!",
        "8, 新的一天又开始了，祝你过得快乐!",
        "9, 新的一天又开始了，祝你过得快乐!",
        "10, 新的一天又开始了，祝你过得快乐!",
        "11, 该吃午饭啦！有什么好吃的？您有中午休息的好习惯吗？",
        "12, 该吃午饭啦！有什么好吃的？您有中午休息的好习惯吗？",
        "13, 该吃午饭啦！有什么好吃的？您有中午休息的好习惯吗？",
        "14, 下午好！外面的天气好吗？记得朵朵白云曾捎来朋友殷殷的祝福。",
        "15, 下午好！外面的天气好吗？记得朵朵白云曾捎来朋友殷殷的祝福。",
        "16, 下午好！外面的天气好吗？记得朵朵白云曾捎来朋友殷殷的祝福。",
        "17, 太阳落山了！快看看夕阳吧！如果外面下雨，就不必了 ^_^",
        "18, 太阳落山了！快看看夕阳吧！如果外面下雨，就不必了 ^_^",
        "19, 晚上好，小伙伴今天的心情怎么样？",
        "20, 晚上好，小伙伴今天的心情怎么样？",
        "21, 晚上好，小伙伴今天的心情怎么样？",
        "22, 这么晚了，小伙伴还在上网？早点洗洗睡吧，睡前记得洗洗脸喔！",
        "23, 这么晚了，小伙伴还在上网？早点洗洗睡吧，睡前记得洗洗脸喔！",
    )
    fun `loadTimeTalk returns correct message for hour`(
        hour: Int,
        expectedMessage: String,
    ) = runTest(testDispatcher) {
        mockkStatic(LocalTime::class)
        every { LocalTime.now() } returns LocalTime.of(hour, 0)

        viewModel = HomeViewModel(portalRepository, settingsRepository)

        assertEquals(expectedMessage, viewModel.timeTalk.value)
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

    /**
     * 测试：初始化时加载多个分类，其中部分分类详情加载失败
     */
    @Test
    fun `init loads portal infos with partial failure`() =
        runTest(testDispatcher) {
            val categories =
                listOf(
                    PortalCategoryItem("1", "Category 1"),
                    PortalCategoryItem("2", "Category 2"),
                )
            val infos1 = listOf(PortalInfoItem("Title1", "Author1", "2025-01-01", "url1"))
            val failure = Failure.IOError("Error fetching infos", null)

            coEvery { portalRepository.getPortalCategory("TestToken") } returns
                PortalRepository.PortalData.Success(categories)
            coEvery { portalRepository.getPortalInfoList("1") } returns PortalRepository.PortalData.Success(infos1)
            coEvery { portalRepository.getPortalInfoList("2") } returns PortalRepository.PortalData.Failed(failure)

            mockkStatic(LocalTime::class)
            every { LocalTime.now() } returns LocalTime.of(10, 0)

            viewModel = HomeViewModel(portalRepository, settingsRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(categories, viewModel.portalCategoryList.value)
            // portalInfoList should contain valid list for successful call and empty list for failed one
            val expectedInfos = listOf(infos1, emptyList())
            assertEquals(expectedInfos, viewModel.portalInfoList.value)
            assertTrue(viewModel.portalState.value is HomeViewModel.PortalState.Success)
        }

    /**
     * 测试：changePortalState 能够正确更新状态
     */
    @Test
    fun `changePortalState updates state correctly`() =
        runTest(testDispatcher) {
            mockkStatic(LocalTime::class)
            every { LocalTime.now() } returns LocalTime.of(10, 0)

            viewModel = HomeViewModel(portalRepository, settingsRepository)

            viewModel.changePortalState(HomeViewModel.PortalState.Loading)
            assertEquals(HomeViewModel.PortalState.Loading, viewModel.portalState.value)

            viewModel.changePortalState(HomeViewModel.PortalState.Idle)
            assertEquals(HomeViewModel.PortalState.Idle, viewModel.portalState.value)
        }
}
