package top.goodboyboy.wolfassistant.ui.firstpage

import android.app.Application
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.jwt.JWT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.ScreenRoute
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.appsetting.GlobalInitConfig
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.LabScheduleRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository
import top.goodboyboy.wolfassistant.util.CacheUtil
import java.net.URLEncoder
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

        private val _hasPassword = MutableStateFlow(false)
        val hasPassword: StateFlow<Boolean> = _hasPassword.asStateFlow()

        private val _hasTokenExpired = MutableStateFlow(true)
        val hasTokenExpired: StateFlow<Boolean> = _hasTokenExpired.asStateFlow()

        private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
        val loadState: StateFlow<LoadState> get() = _loadState.asStateFlow()

        private val _navEvent = Channel<FirstPageEvent>()
        val navEvent = _navEvent.receiveAsFlow()

        sealed class LoadState {
            object Idle : LoadState()

            object Loading : LoadState()

            object Success : LoadState()

            data class Failed(
                val message: String,
            ) : LoadState()
        }

        sealed class FirstPageEvent {
            data object NavigateToLogin : FirstPageEvent()

            data object NavigateToHome : FirstPageEvent()

            data class NavigateToDeepLink(
                val routes: List<String>,
            ) : FirstPageEvent()

            data object ShowTokenExpiredDialog : FirstPageEvent()
        }

        @VisibleForTesting
        internal suspend fun checkLoginStatue() {
            val accessToken = settingsRepository.getAccessTokenDecrypted()
            _hasAccessToken.value = accessToken.isNotEmpty()
            val password = settingsRepository.getUserPasswordDecrypted()
            _hasPassword.value = password.isNotEmpty()
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

        fun handleNav(intent: Intent?) {
            val data = intent?.data
            val isExpired = hasTokenExpired.value
            val hasToken = hasAccessToken.value
            val hasPassword = hasPassword.value

            if (!hasPassword) {
                intent?.data = null
                sendEvent(FirstPageEvent.NavigateToLogin)
                return
            }
            if (!hasToken) {
                intent?.data = null
                sendEvent(FirstPageEvent.NavigateToLogin)
                return
            }
            if (isExpired) {
                intent?.data = null
                sendEvent(FirstPageEvent.ShowTokenExpiredDialog)
                return
            }

            if (data == null) {
                intent?.data = null
                sendEvent(FirstPageEvent.NavigateToHome)
                return
            }

            if (data.scheme == "wolfassistant" && data.host == "scan") {
                // 先去 Home，再去 Scanner
                intent.data = null
                sendEvent(FirstPageEvent.NavigateToDeepLink(listOf(ScreenRoute.Home.route, "scanner")))
            } else if (data.scheme == "wolfassistant" && data.host == "payment-code") {
                intent.data = null
                val url =
                    "https://v8mobile.hut.edu.cn/zdRedirect/toSingleMenu?code=openVirtualcard"
                val encodeUrl = URLEncoder.encode(url, "UTF-8")
                val urlTokenKeyName = "X-Id-Token"
                sendEvent(
                    FirstPageEvent.NavigateToDeepLink(
                        listOf(ScreenRoute.Home.route, "browser/$encodeUrl?urlTokenKeyName=$urlTokenKeyName"),
                    ),
                )
            } else if (data.scheme == "wolfassistant" && data.host == "recharge") {
                intent.data = null
                val url =
                    "https://hub.17wanxiao.com/bsacs/light.action?flag=supwisdomapp_hngydxsw&ecardFunc=recharge"
                val encodeUrl = URLEncoder.encode(url, "UTF-8")
                val urlTokenKeyName = "token"
                sendEvent(
                    FirstPageEvent.NavigateToDeepLink(
                        listOf(ScreenRoute.Home.route, "browser/$encodeUrl?urlTokenKeyName=$urlTokenKeyName"),
                    ),
                )
            } else if (data.scheme == "wolfassistant" && data.host == "campus-card") {
                intent.data = null
                val url = "https://v8mobile.hut.edu.cn/homezzdx/openHomePage"
                val encodeUrl = URLEncoder.encode(url, "UTF-8")
                val urlTokenKeyName = "X-Id-Token"
                sendEvent(
                    FirstPageEvent.NavigateToDeepLink(
                        listOf(ScreenRoute.Home.route, "browser/$encodeUrl?urlTokenKeyName=$urlTokenKeyName"),
                    ),
                )
            } else if (data.scheme == "wolfassistant" && data.host == "schedule") {
                intent.data = null
                sendEvent(FirstPageEvent.NavigateToDeepLink(listOf(ScreenRoute.Schedule.route)))
            } else {
                intent.data = null
                sendEvent(FirstPageEvent.NavigateToHome)
            }
        }

        private fun sendEvent(event: FirstPageEvent) {
            viewModelScope.launch {
                _navEvent.send(event)
            }
        }

        init {
            viewModelScope.launch {
                initAPP()
            }
        }
    }
