package top.goodboyboy.wolfassistant.ui.firstpage

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.jwt.JWT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.appsetting.GlobalInitConfig
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.LabScheduleRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository
import top.goodboyboy.wolfassistant.util.CacheUtil
import javax.inject.Inject

@HiltViewModel
class FirstPageViewModel
    @Inject
    constructor(
        private val portalRepository: PortalRepository,
        private val serviceRepository: ServiceRepository,
        private val scheduleRepository: ScheduleRepository,
        private val labScheduleRepository: LabScheduleRepository,
        private val personalInfoRepository: PersonalInfoRepository,
        private val settingsRepository: SettingsRepository,
        private val application: Application,
    ) : ViewModel() {
        private val _hasAccessToken = MutableStateFlow(false)
        val hasAccessToken: StateFlow<Boolean> = _hasAccessToken.asStateFlow()

        private val _hasTokenExpired = MutableStateFlow(false)
        val hasTokenExpired: StateFlow<Boolean> = _hasTokenExpired.asStateFlow()

        private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
        val loadState: StateFlow<LoadState> get() = _loadState.asStateFlow()

        sealed class LoadState {
            object Idle : LoadState()

            object Loading : LoadState()

            object Success : LoadState()

            data class Failed(
                val message: String,
            ) : LoadState()
        }

        @VisibleForTesting
        internal suspend fun checkLoginStatue() {
            val accessToken = settingsRepository.accessTokenFlow.first()
            _hasAccessToken.value = accessToken.isNotEmpty()
            if (accessToken.isNotEmpty()) {
                val isExpired = JWT(accessToken).isExpired(0)
                _hasTokenExpired.value = isExpired
            }
        }

        private suspend fun initGlobalConfig() {
            val disableSSL = settingsRepository.disableSSLCertVerification.first()
            val onlyIPv4 = settingsRepository.onlyIPv4.first()
            GlobalInitConfig.setConfig(
                disableSSL,
                onlyIPv4,
            )
        }

        private suspend fun initAPP() {
            try {
                _loadState.value = LoadState.Loading
                checkLoginStatue()
                initGlobalConfig()
                _loadState.value = LoadState.Success
            } catch (e: Exception) {
                _loadState.value = LoadState.Failed(e.message ?: "Unknown Error")
            }
        }

        suspend fun logout() {
            portalRepository.cleanCache()
            serviceRepository.cleanServiceList()
            scheduleRepository.cleanScheduleCache()
            labScheduleRepository.cleanLabScheduleCache()
            personalInfoRepository.cleanPersonalInfoCache()
            settingsRepository.cleanUser()
            CacheUtil.clearAllCache(application)
        }

        init {
            viewModelScope.launch {
                initAPP()
            }
        }
    }
