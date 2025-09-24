package top.goodboyboy.wolfassistant.ui.firstpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.appsetting.GlobalInitConfig
import javax.inject.Inject

@HiltViewModel
class FirstPageViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _hasAccessToken = MutableStateFlow(false)
        val hasAccessToken: StateFlow<Boolean> = _hasAccessToken.asStateFlow()

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

        suspend fun checkLoginStatue() {
            val accessToken = settingsRepository.accessTokenFlow.first()
            _hasAccessToken.value = accessToken.isNotEmpty()
        }

        suspend fun initGlobalConfig() {
            val disableSSL = settingsRepository.disableSSLCertVerification.first()
            val onlyIPv4 = settingsRepository.onlyIPv4.first()
            GlobalInitConfig.setConfig(
                disableSSL,
                onlyIPv4,
            )
        }

        suspend fun initAPP() {
            _loadState.value = LoadState.Loading
            checkLoginStatue()
            initGlobalConfig()
            _loadState.value = LoadState.Success
        }

        init {
            viewModelScope.launch {
                initAPP()
            }
        }
    }
