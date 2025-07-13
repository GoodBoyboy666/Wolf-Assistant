package top.goodboyboy.hutassistant.ui.sanner

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.goodboyboy.hutassistant.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel
    @Inject
    constructor(
        settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val accessToken = mutableStateOf("")
        private val _accessTokenLoadState =
            MutableStateFlow<AccessTokenLoadState>(AccessTokenLoadState.Idle)
        val accessTokenLoadState: StateFlow<AccessTokenLoadState> = _accessTokenLoadState.asStateFlow()

        sealed class AccessTokenLoadState {
            object Idle : AccessTokenLoadState()

            object Loading : AccessTokenLoadState()

            object Success : AccessTokenLoadState()

            data class Error(
                val error: String,
            ) : AccessTokenLoadState()
        }

        init {
            _accessTokenLoadState.value = AccessTokenLoadState.Loading
            viewModelScope.launch {
                settingsRepository.accessTokenFlow.collect { data ->
                    accessToken.value = data
                    _accessTokenLoadState.value = AccessTokenLoadState.Success
                }
            }
        }
    }
