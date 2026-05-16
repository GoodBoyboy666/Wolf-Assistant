package top.goodboyboy.wolfassistant.ui.sanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel
    @Inject
    constructor(
        settingsRepository: SettingsRepository,
        private val logger: AppLogger,
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
            logger.i("初始化扫码器")
            _initState.value = InitState.Loading
            viewModelScope.launch {
                val accessToken = settingsRepository.getAccessTokenDecrypted()
                if (accessToken.isNotEmpty()) {
                    logger.i("扫码器初始化完成")
                    _initState.value = InitState.Success
                } else {
                    logger.e(null, "令牌为空，无法初始化扫码器")
                    _initState.value = InitState.Error("Access token 为空")
                }
            }
        }
    }
