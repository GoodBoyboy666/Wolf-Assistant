package top.goodboyboy.wolfassistant.ui.appsetting

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import top.goodboyboy.wolfassistant.BuildConfig
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
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
import javax.inject.Inject

@HiltViewModel
class SettingViewModel
    @Inject
    constructor(
        private val portalRepository: PortalRepository,
        private val serviceRepository: ServiceRepository,
        private val scheduleRepository: ScheduleRepository,
        private val personalInfoRepository: PersonalInfoRepository,
        private val settingsRepository: SettingsRepository,
        private val appSettingRepository: AppSettingRepository,
        private val labScheduleRepository: LabScheduleRepository,
        application: Application,
        private val okHttpClient: OkHttpClient,
        private val logger: AppLogger,
    ) : ViewModel() {
        private val _cacheSize = MutableStateFlow(application.getString(R.string.calculating))
        val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

        private val _updateState = MutableStateFlow<CheckUpdateState>(CheckUpdateState.Idle)
        val updateState: StateFlow<CheckUpdateState> = _updateState.asStateFlow()

        val disableSSLCertVerification = settingsRepository.disableSSLCertVerification
        val onlyIPv4 = settingsRepository.onlyIPv4

        val enablePreRelease = settingsRepository.enablePreRelease

        suspend fun getTotalCacheSize(context: Context) {
            logger.i("计算缓存大小")
            withContext(Dispatchers.IO) {
                val size = CacheUtil.getTotalCacheSize(context)
                withContext(Dispatchers.Main) {
                    _cacheSize.value = size
                }
            }
        }

        suspend fun cleanAllCache(context: Context) {
            logger.i("清除全部缓存")
            withContext(Dispatchers.IO) {
                CacheUtil.clearAllCache(context)
                getTotalCacheSize(context)
            }
        }

        suspend fun logout(context: Context) {
            logger.i("退出登录")
            portalRepository.cleanCache()
            serviceRepository.cleanServiceList()
            scheduleRepository.cleanScheduleCache()
            labScheduleRepository.cleanLabScheduleCache()
            personalInfoRepository.cleanPersonalInfoCache()
            settingsRepository.cleanUser()
            cleanAllCache(context)
        }

        suspend fun getUpdateInfo() {
            logger.i("检查更新")
            _updateState.value = CheckUpdateState.Loading
            val result =
                appSettingRepository.getUpdateInfo(
                    BuildConfig.VERSION_NAME,
                    settingsRepository.enablePreRelease.first(),
                )
            when (result) {
                is VersionDomainData.Error -> {
                    logger.e(result.error.cause, "检查更新失败")
                    _updateState.value = CheckUpdateState.Error(result.error)
                }

                VersionDomainData.NOUpdate -> {
                    _updateState.value = CheckUpdateState.Success(null)
                }

                is VersionDomainData.Success -> {
                    logger.i("发现新版本")
                    _updateState.value = CheckUpdateState.Success(result.data)
                }
            }
        }

        fun changeUpdateState(state: CheckUpdateState) {
            _updateState.value = state
        }

        suspend fun setSSLCertVerification(value: Boolean) {
            logger.i("设置SSL证书验证")
            settingsRepository.setSSLCertVerification(value)
            GlobalInitConfig.setConfig(
                settingsRepository.disableSSLCertVerification.first(),
                settingsRepository.onlyIPv4.first(),
            )
            withContext(Dispatchers.IO) {
                okHttpClient.dispatcher.cancelAll()
                okHttpClient.connectionPool.evictAll()
            }
        }

        suspend fun setOnlyIPv4(value: Boolean) {
            logger.i("设置仅IPv4")
            settingsRepository.setOnlyIPv4(value)
            GlobalInitConfig.setConfig(
                settingsRepository.disableSSLCertVerification.first(),
                settingsRepository.onlyIPv4.first(),
            )
            withContext(Dispatchers.IO) {
                okHttpClient.dispatcher.cancelAll()
                okHttpClient.connectionPool.evictAll()
            }
        }

        suspend fun setEnablePreRelease(value: Boolean) {
            logger.i("设置预发布版本更新")
            settingsRepository.setEnablePreRelease(value)
        }

        sealed class CheckUpdateState {
            object Idle : CheckUpdateState()

            object Loading : CheckUpdateState()

            data class Success(
                val data: VersionInfo?,
            ) : CheckUpdateState()

            data class Error(
                val error: Failure,
            ) : CheckUpdateState()
        }
    }
