package top.goodboyboy.wolfassistant.ui.sanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel
    @Inject
    constructor(
        settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _initState =
            MutableStateFlow<InitState>(InitState.Idle)
        val initState: StateFlow<InitState> = _initState.asStateFlow()

        sealed class InitState {
            object Idle : InitState()

            object Loading : InitState()

            object Success : InitState()

            data class Error(
                val error: String,
            ) : InitState()
        }

        init {
            _initState.value = InitState.Loading
            viewModelScope.launch {
                val accessToken= settingsRepository.accessTokenFlow.first()
                if(accessToken.isNotEmpty()) {
                    _initState.value = InitState.Success
                }else{
                    _initState.value = InitState.Error("Access token 为空")
                }
            }
        }
    }
