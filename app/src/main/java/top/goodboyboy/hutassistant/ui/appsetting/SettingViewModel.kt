package top.goodboyboy.hutassistant.ui.appsetting

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import top.goodboyboy.hutassistant.BuildConfig
import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.settings.SettingsRepository
import top.goodboyboy.hutassistant.ui.appsetting.model.VersionDomainData
import top.goodboyboy.hutassistant.ui.appsetting.model.VersionInfo
import top.goodboyboy.hutassistant.ui.appsetting.repository.AppSettingRepository
import top.goodboyboy.hutassistant.ui.home.portal.repository.PortalRepository
import top.goodboyboy.hutassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import top.goodboyboy.hutassistant.ui.schedulecenter.repository.ScheduleCenterRepository
import top.goodboyboy.hutassistant.ui.servicecenter.service.repository.ServiceRepository
import top.goodboyboy.hutassistant.util.CacheUtil
import javax.inject.Inject

@HiltViewModel
class SettingViewModel
    @Inject
    constructor(
        private val portalRepository: PortalRepository,
        private val serviceRepository: ServiceRepository,
        private val scheduleCenterRepository: ScheduleCenterRepository,
        private val personalInfoRepository: PersonalInfoRepository,
        private val settingsRepository: SettingsRepository,
        private val appSettingRepository: AppSettingRepository,
    ) : ViewModel() {
        private val _cacheSize = MutableStateFlow("正在计算中……")
        val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

        private val _updateState = MutableStateFlow<CheckUpdateState>(CheckUpdateState.Idle)
        val updateState: StateFlow<CheckUpdateState> = _updateState.asStateFlow()

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
            scheduleCenterRepository.cleanScheduleCache()
            personalInfoRepository.cleanPersonalInfoCache()
            settingsRepository.cleanAllData()
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
