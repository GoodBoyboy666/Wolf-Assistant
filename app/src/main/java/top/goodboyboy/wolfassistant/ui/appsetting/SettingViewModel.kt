package top.goodboyboy.wolfassistant.ui.appsetting

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
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
        private val application: Application,
    ) : ViewModel() {
        private val _cacheSize = MutableStateFlow(application.getString(R.string.calculating))
        val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

        private val _updateState = MutableStateFlow<CheckUpdateState>(CheckUpdateState.Idle)
        val updateState: StateFlow<CheckUpdateState> = _updateState.asStateFlow()

        val disableSSLCertVerification = settingsRepository.disableSSLCertVerification
        val onlyIPv4 = settingsRepository.onlyIPv4

        suspend fun getTotalCacheSize(context: Context) {
            withContext(Dispatchers.IO) {
                val size = CacheUtil.getTotalCacheSize(context)
                withContext(Dispatchers.Main) {
                    _cacheSize.value = size
                }
            }
        }

        suspend fun cleanAllCache(context: Context) {
            withContext(Dispatchers.IO) {
                CacheUtil.clearAllCache(context)
                getTotalCacheSize(context)
            }
        }

        suspend fun logout(context: Context) {
            portalRepository.cleanCache()
            serviceRepository.cleanServiceList()
            scheduleRepository.cleanScheduleCache()
            labScheduleRepository.cleanLabScheduleCache()
            personalInfoRepository.cleanPersonalInfoCache()
            settingsRepository.cleanUser()
            cleanAllCache(context)
        }

        suspend fun getUpdateInfo() {
            _updateState.value = CheckUpdateState.Loading
            val result = appSettingRepository.getUpdateInfo(BuildConfig.VERSION_NAME)
            when (result) {
                is VersionDomainData.Error -> {
                    _updateState.value = CheckUpdateState.Error(result.error)
                }

                VersionDomainData.NOUpdate -> {
                    _updateState.value = CheckUpdateState.Success(null)
                }

                is VersionDomainData.Success -> {
                    _updateState.value = CheckUpdateState.Success(result.data)
                }
            }
        }

        fun changeUpdateState(state: CheckUpdateState) {
            _updateState.value = state
        }

        suspend fun setSSLCertVerification(value: Boolean) {
            settingsRepository.setSSLCertVerification(value)
        }

        suspend fun setOnlyIPv4(value: Boolean) {
            settingsRepository.setOnlyIPv4(value)
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
