package top.goodboyboy.wolfassistant.ui.sanner

import io.mockk.every
import io.mockk.mockk
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
import top.goodboyboy.wolfassistant.settings.SettingsRepository

/**
 * ScannerViewModel 的单元测试
 *
 * 测试要点：
 * 1. 验证 ViewModel 初始化时，检查 accessToken 是否存在
 * 2. 如果 accessToken 存在，状态应更新为 Success
 * 3. 如果 accessToken 为空，状态应更新为 Error
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {
    private lateinit var viewModel: ScannerViewModel
    private val settingsRepository: SettingsRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    /**
     * 在每个测试前设置主协程调度器为测试调度器，避免使用默认主线程
     */
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
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
     * 测试：当 AccessToken 不为空时，初始化状态应设为 Success
     */
    @Test
    fun `init should set Success state when access token is not empty`() =
        runTest(testDispatcher) {
            // 准备数据：模拟 SettingsRepository 返回有效的 token
            val token = "valid-token"
            every { settingsRepository.accessTokenFlow } returns flowOf(token)

            // 执行操作：初始化 ViewModel
            viewModel = ScannerViewModel(settingsRepository)
            // 让协程调度器执行完所有挂起的任务
            testDispatcher.scheduler.advanceUntilIdle()

            // 验证结果：状态应为 Success
            assertTrue(viewModel.initState.value is ScannerViewModel.InitState.Success)
        }

    /**
     * 测试：当 AccessToken 为空时，初始化状态应设为 Error
     */
    @Test
    fun `init should set Error state when access token is empty`() =
        runTest(testDispatcher) {
            // 准备数据：模拟 SettingsRepository 返回空 token
            val token = ""
            every { settingsRepository.accessTokenFlow } returns flowOf(token)

            // 执行操作：初始化 ViewModel
            viewModel = ScannerViewModel(settingsRepository)
            // 让协程调度器执行完所有挂起的任务
            testDispatcher.scheduler.advanceUntilIdle()

            // 验证结果：状态应为 Error，且包含错误信息
            val state = viewModel.initState.value
            assertTrue(state is ScannerViewModel.InitState.Error)
            assertEquals("Access token 为空", (state as ScannerViewModel.InitState.Error).error)
        }
}
