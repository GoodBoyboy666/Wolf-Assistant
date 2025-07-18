package top.goodboyboy.hutassistant.ui.webview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import top.goodboyboy.hutassistant.common.Event
import top.goodboyboy.hutassistant.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        val accessTokenStateFlow =
            settingsRepository.accessTokenFlow.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )
        private val _refreshEvent = MutableStateFlow<Event<Unit>?>(null)
        val refreshEvent = _refreshEvent.asStateFlow()

        fun onRefresh() {
            _refreshEvent.value = Event(Unit)
        }
    }
