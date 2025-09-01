package top.goodboyboy.wolfassistant.ui.schedulecenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleCenterRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleCenterRepository.ScheduleData.Failed
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleCenterRepository.ScheduleData.Success
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleCenterViewModel
    @Inject
    constructor(
        private val scheduleCenterRepository: ScheduleCenterRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _loadScheduleState = MutableStateFlow<LoadScheduleState>(LoadScheduleState.Idle)
        val loadScheduleState: StateFlow<LoadScheduleState> = _loadScheduleState.asStateFlow()

        private val _scheduleList = MutableStateFlow<List<ScheduleItem?>>(emptyList())
        val scheduleList: StateFlow<List<ScheduleItem?>> = _scheduleList.asStateFlow()

        sealed class LoadScheduleState {
            object Idle : LoadScheduleState()

            object Loading : LoadScheduleState()

            object Success : LoadScheduleState()

            data class Failed(
                val message: String,
            ) : LoadScheduleState()
        }

        init {
            viewModelScope.launch {
                _loadScheduleState.value = LoadScheduleState.Loading
            }
        }

        suspend fun loadSchedule(
            startDate: LocalDate,
            endDate: LocalDate,
        ) {
            val accessToken = settingsRepository.accessTokenFlow.first()
            val data =
                scheduleCenterRepository.getSchedule(
                    accessToken,
                    settingsRepository.disableSSLCertVerification.first(),
                    startDate,
                    endDate,
                )
            when (data) {
                is Failed -> {
                    _loadScheduleState.value = LoadScheduleState.Failed(data.error.message)
                    data.error.cause?.printStackTrace()
                }

                is Success -> {
                    _scheduleList.value = data.data
                    _loadScheduleState.value = LoadScheduleState.Success
                }
            }
        }

        suspend fun cleanCache() {
            withContext(Dispatchers.IO) {
                scheduleCenterRepository.cleanScheduleCache()
            }
        }

        fun changeState(loadScheduleState: LoadScheduleState) {
            _loadScheduleState.value = loadScheduleState
        }
    }
