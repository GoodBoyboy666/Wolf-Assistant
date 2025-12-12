package top.goodboyboy.wolfassistant.ui.firstpage

import android.app.Application
import android.util.Base64
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.LabScheduleRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository
import top.goodboyboy.wolfassistant.util.CacheUtil
import java.nio.charset.StandardCharsets

/**
 * FirstPageViewModel 的单元测试
 *
 * 测试要点：
 * 1. 验证应用初始化逻辑（Token 检查、过期判断、全局配置加载）
 * 2. 验证登出逻辑（缓存清理）
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirstPageViewModelTest {
    private lateinit var viewModel: FirstPageViewModel
    private lateinit var portalRepository: PortalRepository
    private lateinit var serviceRepository: ServiceRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var labScheduleRepository: LabScheduleRepository
    private lateinit var personalInfoRepository: PersonalInfoRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var application: Application

    private val testDispatcher = StandardTestDispatcher()

    /**
     * 测试前的初始化工作
     * 1. 设置主协程调度器
     * 2. Mock 所有依赖项
     * 3. Mock Android Base64 工具类（因为 JUnit 运行在 JVM 上）
     */
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        portalRepository = mockk(relaxed = true)
        serviceRepository = mockk(relaxed = true)
        scheduleRepository = mockk(relaxed = true)
        labScheduleRepository = mockk(relaxed = true)
        personalInfoRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        application = mockk(relaxed = true)

        // Mock Android Base64 编码/解码
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } answers {
            val input = firstArg<ByteArray>()
            java.util.Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(input)
        }
        every { Base64.decode(any<String>(), any()) } answers {
            val input = firstArg<String>()
            java.util.Base64
                .getUrlDecoder()
                .decode(input)
        }
    }

    /**
     * 测试后的清理工作
     * 重置调度器并取消所有 Mock
     */
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    /**
     * 生成测试用的 JWT token
     * @param expirationTimeSeconds 过期时间（秒），null表示未来很远的时间（未过期）
     */
    private fun generateTestJWT(expirationTimeSeconds: Long? = null): String {
        // 手动构建 JSON 字符串，避免使用 Android 的 JSONObject
        val header = """{"alg":"HS256","typ":"JWT"}"""

        val currentTimeSeconds = System.currentTimeMillis() / 1000
        val expTime = expirationTimeSeconds ?: (currentTimeSeconds + 3153600000L)
        val payload = """{"sub":"1234567890","name":"Test User","iat":$currentTimeSeconds,"exp":$expTime}"""

        val headerEncoded =
            java.util.Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(header.toByteArray(StandardCharsets.UTF_8))
        val payloadEncoded =
            java.util.Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.toByteArray(StandardCharsets.UTF_8))

        // 简化的签名（实际测试中不需要验证签名）
        val signature = "test-signature"

        return "$headerEncoded.$payloadEncoded.$signature"
    }

    /**
     * 测试：当 Token 为空时，应设置 hasAccessToken 为 false 并将 loadState 设为 Success
     */
    @Test
    fun `init should set hasAccessToken to false and loadState to Success when token is empty`() =
        runTest(testDispatcher) {
            // 准备: 空 token
            every { settingsRepository.accessTokenFlow } returns flowOf("")
            every { settingsRepository.disableSSLCertVerification } returns flowOf(false)
            every { settingsRepository.onlyIPv4 } returns flowOf(false)

            // 执行: 创建 ViewModel（会自动触发 init 块中的 initAPP）
            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleRepository,
                    labScheduleRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // 验证: hasAccessToken 应为 false，loadState 应为 Success
            viewModel.hasAccessToken.test {
                assertFalse(awaitItem())
            }
            viewModel.hasTokenExpired.test {
                assertFalse(awaitItem())
            }
            viewModel.loadState.test {
                assertTrue(awaitItem() is FirstPageViewModel.LoadState.Success)
            }
        }

    /**
     * 测试：当 Token 存在且未过期时，应设置 hasAccessToken 为 true
     */
    @Test
    fun `init should set hasAccessToken to true when token exists and not expired`() =
        runTest(testDispatcher) {
            // 准备: 动态生成一个未过期的 JWT token
            val validToken = generateTestJWT()
            every { settingsRepository.accessTokenFlow } returns flowOf(validToken)
            every { settingsRepository.disableSSLCertVerification } returns flowOf(false)
            every { settingsRepository.onlyIPv4 } returns flowOf(false)

            // 执行: 创建 ViewModel 触发 init 块
            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleRepository,
                    labScheduleRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // 验证: hasAccessToken 应为 true，hasTokenExpired 为 false
            viewModel.hasAccessToken.test {
                assertTrue(awaitItem())
            }
            viewModel.hasTokenExpired.test {
                assertFalse(awaitItem())
            }
            viewModel.loadState.test {
                assertTrue(awaitItem() is FirstPageViewModel.LoadState.Success)
            }
        }

    /**
     * 测试：当 Token 已过期时，应设置 hasTokenExpired 为 true
     */
    @Test
    fun `init should detect expired token and set hasTokenExpired to true`() =
        runTest(testDispatcher) {
            // 准备: 动态生成一个已过期的 JWT token（设置过期时间为 1 年前）
            val oneYearAgoInSeconds = System.currentTimeMillis() / 1000 - 31536000
            val expiredToken = generateTestJWT(oneYearAgoInSeconds)
            every { settingsRepository.accessTokenFlow } returns flowOf(expiredToken)
            every { settingsRepository.disableSSLCertVerification } returns flowOf(false)
            every { settingsRepository.onlyIPv4 } returns flowOf(false)

            // 执行: 创建 ViewModel 触发 init 块
            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleRepository,
                    labScheduleRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // 验证: hasAccessToken 为 true，hasTokenExpired 也为 true
            viewModel.hasAccessToken.test {
                assertTrue(awaitItem())
            }
            viewModel.hasTokenExpired.test {
                assertTrue(awaitItem())
            }
            viewModel.loadState.test {
                assertTrue(awaitItem() is FirstPageViewModel.LoadState.Success)
            }
        }

    /**
     * 测试：初始化过程发生异常时，loadState 应变为 Failed
     */
    @Test
    fun `init should set loadState to Failed when exception occurs`() =
        runTest(testDispatcher) {
            // 准备: settingsRepository 抛出异常
            val errorMessage = "Network error"
            every { settingsRepository.accessTokenFlow } throws RuntimeException(errorMessage)
            every { settingsRepository.disableSSLCertVerification } returns flowOf(false)
            every { settingsRepository.onlyIPv4 } returns flowOf(false)

            // 执行: 创建 ViewModel 触发 init 块
            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleRepository,
                    labScheduleRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // 验证: loadState 应该为 Failed，包含错误信息
            viewModel.loadState.test {
                val state = awaitItem()
                assertTrue(state is FirstPageViewModel.LoadState.Failed)
                assertTrue((state as FirstPageViewModel.LoadState.Failed).message.contains(errorMessage))
            }
        }

    /**
     * 测试：应正确初始化全局配置（SSL 和 IPv4 设置）
     */
    @Test
    fun `init should initialize global config with SSL and IPv4 settings`() =
        runTest(testDispatcher) {
            // 准备: 配置 SSL 和 IPv4 设置
            val disableSSL = true
            val onlyIPv4 = true
            every { settingsRepository.accessTokenFlow } returns flowOf("")
            every { settingsRepository.disableSSLCertVerification } returns flowOf(disableSSL)
            every { settingsRepository.onlyIPv4 } returns flowOf(onlyIPv4)

            // 执行: 创建 ViewModel 触发 init 块（会调用 initGlobalConfig）
            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleRepository,
                    labScheduleRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // 验证: 应该成功完成初始化，loadState 为 Success
            viewModel.loadState.test {
                assertTrue(awaitItem() is FirstPageViewModel.LoadState.Success)
            }
        }

    /**
     * 测试：退出登录应清除所有缓存和用户数据
     */
    @Test
    fun `logout should clear all caches`() =
        runTest(testDispatcher) {
            // 准备: Mock CacheUtil
            io.mockk.mockkObject(CacheUtil)
            every { CacheUtil.clearAllCache(any()) } just Runs

            every { settingsRepository.accessTokenFlow } returns flowOf("")
            every { settingsRepository.disableSSLCertVerification } returns flowOf(false)
            every { settingsRepository.onlyIPv4 } returns flowOf(false)

            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleRepository,
                    labScheduleRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // 执行: 调用 logout
            viewModel.logout()
            testDispatcher.scheduler.advanceUntilIdle()

            // 验证: 所有 repository 的清理方法被调用
            coVerify(exactly = 1) { portalRepository.cleanCache() }
            coVerify(exactly = 1) { serviceRepository.cleanServiceList() }
            coVerify(exactly = 1) { scheduleRepository.cleanScheduleCache() }
            coVerify(exactly = 1) { labScheduleRepository.cleanLabScheduleCache() }
            coVerify(exactly = 1) { personalInfoRepository.cleanPersonalInfoCache() }
            coVerify(exactly = 1) { settingsRepository.cleanUser() }
            verify(exactly = 1) { CacheUtil.clearAllCache(application) }
        }
}
