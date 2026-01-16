package top.goodboyboy.wolfassistant.ui.personalcenter

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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.model.PersonalInfo
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository

/**
 * PersonalCenterViewModel 的单元测试
 *
 * 测试要点：
 * 1. 验证加载个人信息成功时，状态更新为 Success 并且 personalInfo 数据正确
 * 2. 验证加载个人信息失败时，状态更新为 Failed 并且 personalInfo 保持为空
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PersonalCenterViewModelTest {
    private lateinit var viewModel: PersonalCenterViewModel
    private lateinit var personalInfoRepository: PersonalInfoRepository
    private lateinit var settingsRepository: SettingsRepository

    // 使用标准测试调度器，用于控制协程执行顺序
    private val testDispatcher = StandardTestDispatcher()

    /**
     * 测试前准备：
     * 1. 设置 Main Dispatcher 为测试调度器
     * 2. 初始化 Mock 对象
     */
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // relaxed = true 允许 Mock 对象在未定义行为时返回默认值
        personalInfoRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
    }

    /**
     * 测试后清理：
     * 1. 重置 Main Dispatcher
     * 2. 清理所有 Mock 对象
     */
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    /**
     * 测试：加载个人信息成功
     *
     * 预期行为：
     * - loadState 更新为 Success
     * - personalInfo 更新为预期的数据
     */
    @Test
    fun `loadPersonalInfo success_should update personalInfo and set loadState to Success`() =
        runTest(testDispatcher) {
            // 准备数据：模拟有效的 token 和成功的用户信息返回
            val token = "token"
            val expected =
                PersonalInfo(
                    userUid = "uid",
                    userName = "name",
                    organizationName = "org",
                    identityTypeName = "student",
                    imageUrl = "",
                )
            // 模拟 settingsRepository 返回 token
            coEvery { settingsRepository.getAccessTokenDecrypted() } returns token
            // 模拟 personalInfoRepository 返回成功数据
            coEvery { personalInfoRepository.getPersonalInfo(token) } returns
                PersonalInfoRepository.PersonalInfoData.Success(expected)

            // 执行操作：初始化 ViewModel（会自动触发加载逻辑）
            viewModel = PersonalCenterViewModel(personalInfoRepository, settingsRepository)
            // 让协程调度器执行完所有挂起的任务
            testDispatcher.scheduler.advanceUntilIdle()

            // 验证结果：
            // 1. 加载状态应为 Success
            assertTrue(viewModel.loadState.value is PersonalCenterViewModel.LoadState.Success)
            // 2. 个人信息应与预期一致
            assertEquals(expected, viewModel.personalInfo.value)
            // 3. 验证仓库方法被调用了一次
            coVerify(exactly = 1) { personalInfoRepository.getPersonalInfo(token) }
        }

    /**
     * 测试：加载个人信息失败
     *
     * 预期行为：
     * - loadState 更新为 Failed
     * - personalInfo 保持为 null
     */
    @Test
    fun `loadPersonalInfo failed should set loadState to Failed and personalInfo remain null`() =
        runTest(testDispatcher) {
            // 准备数据：模拟有效的 token 和失败的返回结果（如网络错误）
            val token = "token"
            val errMsg = "network error"
            coEvery { settingsRepository.getAccessTokenDecrypted() } returns token
            // 模拟 personalInfoRepository 返回失败数据
            coEvery { personalInfoRepository.getPersonalInfo(token) } returns
                PersonalInfoRepository.PersonalInfoData.Failed(Failure.IOError(errMsg, null))

            // 执行操作：初始化 ViewModel
            viewModel = PersonalCenterViewModel(personalInfoRepository, settingsRepository)
            // 让协程调度器执行完所有挂起的任务
            testDispatcher.scheduler.advanceUntilIdle()

            // 验证结果：
            // 1. 加载状态应为 Failed
            assertTrue(viewModel.loadState.value is PersonalCenterViewModel.LoadState.Failed)
            val state = viewModel.loadState.value as PersonalCenterViewModel.LoadState.Failed
            // 2. 错误信息包含预期的字符串
            assertTrue(state.reason.contains(errMsg))
            // 3. 个人信息应为 null
            assertNull(viewModel.personalInfo.value)
            // 4. 验证仓库方法被调用了一次
            coVerify(exactly = 1) { personalInfoRepository.getPersonalInfo(token) }
        }
}
