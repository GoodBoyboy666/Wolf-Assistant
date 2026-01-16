package top.goodboyboy.wolfassistant.ui.webview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.common.Event
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        sealed class LoadState {
            object Idle : LoadState()

            object Loading : LoadState()

            data class Success(
                val accessToken: String,
            ) : LoadState()

            data class Failed(
                val message: String,
            ) : LoadState()
        }

        private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
        val loadState = _loadState.asStateFlow()
        private val _refreshEvent = MutableStateFlow<Event<Unit>?>(null)
        val refreshEvent = _refreshEvent.asStateFlow()

        fun onRefresh() {
            _refreshEvent.value = Event(Unit)
        }

        init {
            viewModelScope.launch {
                _loadState.value = LoadState.Loading
                val accessToken = settingsRepository.getAccessTokenDecrypted()
                if (accessToken.isNotEmpty()) {
                    _loadState.value = LoadState.Success(accessToken)
                } else {
                    _loadState.value = LoadState.Failed("No access token found")
                }
            }
        }
    }
