package top.goodboyboy.wolfassistant.ui.firstpage

import android.app.Application
import android.util.Base64
import app.cash.turbine.test
import io.mockk.coEvery
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleCenterRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalCoroutinesApi::class)
class FirstPageViewModelTest {
    private lateinit var viewModel: FirstPageViewModel
    private lateinit var portalRepository: PortalRepository
    private lateinit var serviceRepository: ServiceRepository
    private lateinit var scheduleCenterRepository: ScheduleCenterRepository
    private lateinit var personalInfoRepository: PersonalInfoRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var application: Application

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        portalRepository = mockk(relaxed = true)
        serviceRepository = mockk(relaxed = true)
        scheduleCenterRepository = mockk(relaxed = true)
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

    @Test
    fun `init should set hasAccessToken to false and loadState to Success when token is empty`() =
        runTest(testDispatcher) {
            // Given: 空token
            coEvery { settingsRepository.accessTokenFlow } returns flowOf("")
            coEvery { settingsRepository.disableSSLCertVerification } returns flowOf(false)
            coEvery { settingsRepository.onlyIPv4 } returns flowOf(false)

            // When: 创建ViewModel（会自动触发init块中的initAPP）
            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleCenterRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: hasAccessToken应该为false，loadState应该为Success
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

    @Test
    fun `init should set hasAccessToken to true when token exists and not expired`() =
        runTest(testDispatcher) {
            // Given: 动态生成一个未过期的JWT token
            val validToken = generateTestJWT()
            coEvery { settingsRepository.accessTokenFlow } returns flowOf(validToken)
            coEvery { settingsRepository.disableSSLCertVerification } returns flowOf(false)
            coEvery { settingsRepository.onlyIPv4 } returns flowOf(false)

            // When: 创建ViewModel触发init块
            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleCenterRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: hasAccessToken应该为true，hasTokenExpired为false，loadState为Success
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

    @Test
    fun `init should detect expired token and set hasTokenExpired to true`() =
        runTest(testDispatcher) {
            // Given: 动态生成一个已过期的JWT token（设置过期时间为1年前）
            val oneYearAgoInSeconds = System.currentTimeMillis() / 1000 - 31536000
            val expiredToken = generateTestJWT(oneYearAgoInSeconds)
            coEvery { settingsRepository.accessTokenFlow } returns flowOf(expiredToken)
            coEvery { settingsRepository.disableSSLCertVerification } returns flowOf(false)
            coEvery { settingsRepository.onlyIPv4 } returns flowOf(false)

            // When: 创建ViewModel触发init块
            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleCenterRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: hasAccessToken为true，hasTokenExpired也为true
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

    @Test
    fun `init should set loadState to Failed when exception occurs`() =
        runTest(testDispatcher) {
            // Given: settingsRepository抛出异常
            val errorMessage = "Network error"
            coEvery { settingsRepository.accessTokenFlow } throws RuntimeException(errorMessage)
            coEvery { settingsRepository.disableSSLCertVerification } returns flowOf(false)
            coEvery { settingsRepository.onlyIPv4 } returns flowOf(false)

            // When: 创建ViewModel触发init块
            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleCenterRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: loadState应该为Failed，包含错误信息
            viewModel.loadState.test {
                val state = awaitItem()
                assertTrue(state is FirstPageViewModel.LoadState.Failed)
                assertTrue((state as FirstPageViewModel.LoadState.Failed).message.contains(errorMessage))
            }
        }

    @Test
    fun `init should initialize global config with SSL and IPv4 settings`() =
        runTest(testDispatcher) {
            // Given: 配置SSL和IPv4设置
            val disableSSL = true
            val onlyIPv4 = true
            coEvery { settingsRepository.accessTokenFlow } returns flowOf("")
            coEvery { settingsRepository.disableSSLCertVerification } returns flowOf(disableSSL)
            coEvery { settingsRepository.onlyIPv4 } returns flowOf(onlyIPv4)

            // When: 创建ViewModel触发init块（会调用initGlobalConfig）
            viewModel =
                FirstPageViewModel(
                    portalRepository,
                    serviceRepository,
                    scheduleCenterRepository,
                    personalInfoRepository,
                    settingsRepository,
                    application,
                )
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: 应该成功完成初始化，loadState为Success
            viewModel.loadState.test {
                assertTrue(awaitItem() is FirstPageViewModel.LoadState.Success)
            }
        }
}
