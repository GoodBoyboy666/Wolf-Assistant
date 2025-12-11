package top.goodboyboy.wolfassistant.ui.webview

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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.settings.SettingsRepository

/**
 * BrowserViewModel 的单元测试
 *
 * 测试要点：
 * 1. 验证初始化时加载 AccessToken 成功的场景
 * 2. 验证初始化时加载 AccessToken 失败（为空）的场景
 * 3. 验证刷新事件触发
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrowserViewModelTest {
    private lateinit var viewModel: BrowserViewModel
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    /**
     * 测试：初始化成功
     * 预期：当 token 存在时，状态变为 Success
     */
    @Test
    fun `init success should set Success state with token`() =
        runTest(testDispatcher) {
            // Arrange
            val token = "valid-token"
            every { settingsRepository.accessTokenFlow } returns flowOf(token)

            // Act
            viewModel = BrowserViewModel(settingsRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            val state = viewModel.loadState.value
            assertTrue(state is BrowserViewModel.LoadState.Success)
            assertEquals(token, (state as BrowserViewModel.LoadState.Success).accessToken)
        }

    /**
     * 测试：初始化失败
     * 预期：当 token 为空时，状态变为 Failed
     */
    @Test
    fun `init failure should set Failed state when token is empty`() =
        runTest(testDispatcher) {
            // Arrange
            val token = ""
            every { settingsRepository.accessTokenFlow } returns flowOf(token)

            // Act
            viewModel = BrowserViewModel(settingsRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            // Assert
            val state = viewModel.loadState.value
            assertTrue(state is BrowserViewModel.LoadState.Failed)
            assertEquals("No access token found", (state as BrowserViewModel.LoadState.Failed).message)
        }

    /**
     * 测试：刷新事件
     * 预期：调用 onRefresh 后，refreshEvent 不为空
     */
    @Test
    fun `onRefresh should trigger refresh event`() =
        runTest(testDispatcher) {
            // Arrange
            every { settingsRepository.accessTokenFlow } returns flowOf("token")
            viewModel = BrowserViewModel(settingsRepository)

            // Act
            viewModel.onRefresh()

            // Assert
            assertNotNull(viewModel.refreshEvent.value)
        }
}
