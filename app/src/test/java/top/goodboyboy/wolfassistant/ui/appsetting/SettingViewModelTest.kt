package top.goodboyboy.wolfassistant.ui.appsetting

import android.app.Application
import android.content.Context
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import top.goodboyboy.wolfassistant.BuildConfig
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionInfo
import top.goodboyboy.wolfassistant.ui.appsetting.repository.AppSettingRepository
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.LabScheduleRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository
import top.goodboyboy.wolfassistant.util.CacheUtil

/**
 * SettingViewModel 的单元测试类
 *
 * 验证 ViewModel 中的业务逻辑，包括：
 * - 缓存大小获取与清理
 * - 用户登出（清理各模块缓存）
 * - 版本更新检查
 * - 设置项修改（SSL 验证、IPv4 偏好）
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingViewModelTest {
    // 依赖项 Mock
    private lateinit var portalRepository: PortalRepository
    private lateinit var serviceRepository: ServiceRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var labScheduleRepository: LabScheduleRepository
    private lateinit var personalInfoRepository: PersonalInfoRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var appSettingRepository: AppSettingRepository
    private lateinit var application: Application
    private lateinit var context: Context
    private lateinit var okHttpClient: OkHttpClient

    private lateinit var viewModel: SettingViewModel

    // 协程测试调度器
    private val testDispatcher = StandardTestDispatcher()

    /**
     * 测试前初始化
     * 1. 设置主线程调度器为 testDispatcher
     * 2. 创建所有依赖的 Mock 对象 (relaxed = true 以避免未 stub 方法抛出异常)
     * 3. Mock 静态工具类 CacheUtil
     * 4. Stub 必要的初始化调用（Application 字符串资源、SettingsRepository 的 flow）
     * 5. 初始化 ViewModel
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
        appSettingRepository = mockk(relaxed = true)
        application = mockk(relaxed = true)
        context = mockk(relaxed = true)
        okHttpClient = mockk(relaxed = true)

        // Mock 静态类 CacheUtil
        mockkObject(CacheUtil)
        every { CacheUtil.getTotalCacheSize(any()) } returns "10MB"
        every { CacheUtil.clearAllCache(any()) } just Runs

        // Mock 初始化时用到的 Application 资源
        every { application.getString(R.string.calculating) } returns "Calculating..."

        // Mock 初始化时收集的 SettingsRepository flow
        every { settingsRepository.disableSSLCertVerification } returns flowOf(false)
        every { settingsRepository.onlyIPv4 } returns flowOf(false)

        viewModel =
            SettingViewModel(
                portalRepository,
                serviceRepository,
                scheduleRepository,
                personalInfoRepository,
                settingsRepository,
                appSettingRepository,
                labScheduleRepository,
                application,
                okHttpClient,
            )
    }

    /**
     * 测试后清理
     * 1. 解除所有 Mock
     * 2. 重置主线程调度器
     */
    @AfterEach
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    /**
     * 验证 getTotalCacheSize 方法
     * 预期：
     * 1. 调用 CacheUtil.getTotalCacheSize 获取缓存大小
     * 2. cacheSize StateFlow 更新为获取到的值 ("10MB")
     */
    @Test
    fun `getTotalCacheSize updates cacheSize flow`() =
        runTest {
            viewModel.getTotalCacheSize(context)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("10MB", viewModel.cacheSize.value)
            io.mockk.verify { CacheUtil.getTotalCacheSize(context) }
        }

    /**
     * 验证 cleanAllCache 方法
     * 预期：
     * 1. 按顺序调用 CacheUtil.clearAllCache 清理缓存
     * 2. 再次调用 CacheUtil.getTotalCacheSize 刷新缓存大小
     */
    @Test
    fun `cleanAllCache clears cache and updates size`() =
        runTest {
            viewModel.cleanAllCache(context)
            testDispatcher.scheduler.advanceUntilIdle()

            io.mockk.verifyOrder {
                CacheUtil.clearAllCache(context)
                CacheUtil.getTotalCacheSize(context)
            }
        }

    /**
     * 验证 logout 方法
     * 预期：
     * 1. 调用各 Repository 的清理方法清理业务数据
     * 2. 清理用户相关设置 (settingsRepository.cleanUser)
     * 3. 清理应用物理缓存 (CacheUtil.clearAllCache)
     */
    @Test
    fun `logout cleans all repositories and cache`() =
        runTest {
            viewModel.logout(context)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify {
                portalRepository.cleanCache()
                serviceRepository.cleanServiceList()
                scheduleRepository.cleanScheduleCache()
                personalInfoRepository.cleanPersonalInfoCache()
                settingsRepository.cleanUser()
            }
            io.mockk.verify { CacheUtil.clearAllCache(context) }
        }

    /**
     * 验证 getUpdateInfo 方法 - 获取更新成功场景
     * 预期：
     * 1. Mock 仓库返回 VersionDomainData.Success
     * 2. updateState 更新为 CheckUpdateState.Success 且包含版本信息
     */
    @Test
    fun `getUpdateInfo success updates state`() =
        runTest {
            val versionInfo = mockk<VersionInfo>()
            coEvery { appSettingRepository.getUpdateInfo(BuildConfig.VERSION_NAME) } returns
                VersionDomainData.Success(versionInfo)

            viewModel.getUpdateInfo()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.updateState.value
            assertTrue(state is SettingViewModel.CheckUpdateState.Success)
            assertEquals(versionInfo, (state as SettingViewModel.CheckUpdateState.Success).data)
        }

    /**
     * 验证 getUpdateInfo 方法 - 无更新场景
     * 预期：
     * 1. Mock 仓库返回 VersionDomainData.NOUpdate
     * 2. updateState 更新为 CheckUpdateState.Success 且数据为 null
     */
    @Test
    fun `getUpdateInfo no update updates state`() =
        runTest {
            coEvery { appSettingRepository.getUpdateInfo(BuildConfig.VERSION_NAME) } returns VersionDomainData.NOUpdate

            viewModel.getUpdateInfo()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.updateState.value
            assertTrue(state is SettingViewModel.CheckUpdateState.Success)
            assertEquals(null, (state as SettingViewModel.CheckUpdateState.Success).data)
        }

    /**
     * 验证 getUpdateInfo 方法 - 获取失败场景
     * 预期：
     * 1. Mock 仓库返回 VersionDomainData.Error
     * 2. updateState 更新为 CheckUpdateState.Error 且包含错误信息
     */
    @Test
    fun `getUpdateInfo error updates state`() =
        runTest {
            val failure = mockk<Failure>()
            coEvery { appSettingRepository.getUpdateInfo(BuildConfig.VERSION_NAME) } returns
                VersionDomainData.Error(failure)

            viewModel.getUpdateInfo()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.updateState.value
            assertTrue(state is SettingViewModel.CheckUpdateState.Error)
            assertEquals(failure, (state as SettingViewModel.CheckUpdateState.Error).error)
        }

    /**
     * 验证 setSSLCertVerification 方法
     * 预期：
     * 1. 调用 settingsRepository.setSSLCertVerification 更新设置
     */
    @Test
    fun `setSSLCertVerification calls repository`() =
        runTest {
            viewModel.setSSLCertVerification(true)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { settingsRepository.setSSLCertVerification(true) }
        }

    /**
     * 验证 setOnlyIPv4 方法
     * 预期：
     * 1. 调用 settingsRepository.setOnlyIPv4 更新设置
     */
    @Test
    fun `setOnlyIPv4 calls repository`() =
        runTest {
            viewModel.setOnlyIPv4(true)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { settingsRepository.setOnlyIPv4(true) }
        }
}
