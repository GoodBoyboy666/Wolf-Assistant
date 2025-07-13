package top.goodboyboy.hutassistant.ui.firstpage

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import top.goodboyboy.hutassistant.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class FirstPageViewModel
    @Inject
    constructor(
        settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _hasAccessToken = MutableStateFlow(false)
        val hasAccessToken: StateFlow<Boolean> = _hasAccessToken.asStateFlow()

        private val _loadState = mutableStateOf<LoadState>(LoadState.Idle)
        val loadState: State<LoadState> get() = _loadState

        sealed class LoadState {
            object Idle : LoadState()

            object Loading : LoadState()

            object Success : LoadState()

            data class Failed(
                val message: String,
            ) : LoadState()
        }

        init {
            _loadState.value = LoadState.Loading
            viewModelScope.launch {
                val accessToken = settingsRepository.accessTokenFlow.first()
                if (accessToken.isNotEmpty()) {
                    _hasAccessToken.value = true
                } else {
                    _hasAccessToken.value = false
                }
                _loadState.value = LoadState.Success
            }
        }
    }
