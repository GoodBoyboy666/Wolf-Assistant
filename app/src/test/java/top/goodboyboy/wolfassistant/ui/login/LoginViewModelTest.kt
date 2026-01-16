package top.goodboyboy.wolfassistant.ui.login

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import top.goodboyboy.wolfassistant.ui.login.model.UserInfo
import top.goodboyboy.wolfassistant.ui.login.repository.LoginRepository

/**
 * LoginViewModel 的单元测试
 *
 * 测试要点：
 *  - 登录成功时，应将用户信息写入 SettingsRepository 并将状态置为 Success
 *  - 登录失败时，应将状态置为 Failed 且不写入 SettingsRepository
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private lateinit var viewModel: LoginViewModel
    private val loginRepository: LoginRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
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
     * 测试：登录成功应写入 SettingsRepository（userID、organization、userName、accessToken）并将状态设为 Success
     */
    @Test
    fun `login success should update settings and set Success state`() =
        runTest(testDispatcher) {
            val userInfo =
                UserInfo(
                    userID = "user-123",
                    userOrganization = "Org",
                    userName = "TestUser",
                    accessToken = "token-abc",
                )

            coEvery {
                loginRepository.loginUser(any(), any(), any(), any(), any(), any())
            } returns LoginRepository.UserData.Success(userInfo)

            viewModel = LoginViewModel(settingsRepository, loginRepository)

            viewModel.login("id", "passwd")
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Success)

            coVerify(exactly = 1) { settingsRepository.setUserID("user-123") }
            coVerify(exactly = 1) { settingsRepository.setUserOrganization("Org") }
            coVerify(exactly = 1) { settingsRepository.setUserName("TestUser") }
            coVerify(exactly = 1) { settingsRepository.setAccessTokenEncrypted("token-abc") }
        }

    /**
     * 测试：登录失败应将状态设为 Failed，且不应写入 SettingsRepository
     */
    @Test
    fun `login failure should set Failed state and not write settings`() =
        runTest(testDispatcher) {
            val failure = Failure.IOError("登录失败", null)
            coEvery {
                loginRepository.loginUser(any(), any(), any(), any(), any(), any())
            } returns LoginRepository.UserData.Failed(failure)

            viewModel = LoginViewModel(settingsRepository, loginRepository)

            viewModel.login("id", "passwd")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.loginState.value
            assertTrue(state is LoginViewModel.LoginState.Failed)
            assertEquals("登录失败", (state as LoginViewModel.LoginState.Failed).message)

            coVerify(exactly = 0) { settingsRepository.setUserID(any()) }
            coVerify(exactly = 0) { settingsRepository.setUserOrganization(any()) }
            coVerify(exactly = 0) { settingsRepository.setUserName(any()) }
            coVerify(exactly = 0) { settingsRepository.setAccessTokenEncrypted(any()) }
        }
}
