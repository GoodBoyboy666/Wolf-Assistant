package top.goodboyboy.hutassistant.ui.firstpage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import top.goodboyboy.hutassistant.settings.SettingsRepository
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
            _loadState.value = LoadState.Loading
            val accessToken = settingsRepository.accessTokenFlow.first()
            if (accessToken.isNotEmpty()) {
                _hasAccessToken.value = true
            } else {
                _hasAccessToken.value = false
            }
            _loadState.value = LoadState.Success
        }
    }
