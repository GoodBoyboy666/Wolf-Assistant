package top.goodboyboy.wolfassistant.ui.schedulecenter

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.event.RollBackToCurrentDateEvent
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
        globalEventBus: GlobalEventBus,
    ) : ViewModel() {
        companion object {
            const val SCHEDULE_CENTER_TAG = "ScheduleCenter"
        }

        private val _loadScheduleState = MutableStateFlow<LoadScheduleState>(LoadScheduleState.Idle)
        val loadScheduleState: StateFlow<LoadScheduleState> = _loadScheduleState.asStateFlow()

        private val _errorMessage = MutableSharedFlow<String>()
        val errorMessage = _errorMessage.asSharedFlow()

        private val _scheduleList = MutableStateFlow<List<ScheduleItem?>>(emptyList())
        val scheduleList: StateFlow<List<ScheduleItem?>> = _scheduleList.asStateFlow()

        private val _firstDay = MutableStateFlow<LocalDate?>(null)
        val firstDay: StateFlow<LocalDate?> = _firstDay.asStateFlow()

        private val _lastDay = MutableStateFlow<LocalDate?>(null)
        val lastDay: StateFlow<LocalDate?> = _lastDay

        // 监听回到当前日期的事件
        val rollBackToCurrentDateEvent =
            globalEventBus.subscribeToTarget<RollBackToCurrentDateEvent>(
                SCHEDULE_CENTER_TAG,
            )

        sealed class LoadScheduleState {
            object Idle : LoadScheduleState()

            object Loading : LoadScheduleState()

            object Success : LoadScheduleState()

            object Failed : LoadScheduleState()
        }

        suspend fun loadScheduleList() {
            val startDay = firstDay.value
            val endDay = lastDay.value
            if (startDay == null || endDay == null) {
                _loadScheduleState.value = LoadScheduleState.Failed
                _errorMessage.emit("日期不可为Null")
            } else {
                loadSchedule(startDay, endDay)
            }
        }

        private suspend fun loadSchedule(
            startDate: LocalDate,
            endDate: LocalDate,
        ) {
            _loadScheduleState.value = LoadScheduleState.Loading
            val accessToken = settingsRepository.accessTokenFlow.first()
            val data =
                scheduleCenterRepository.getSchedule(
                    accessToken,
                    startDate,
                    endDate,
                )
            when (data) {
                is Failed -> {
                    _loadScheduleState.value = LoadScheduleState.Failed
                    _errorMessage.emit(data.error.message)
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

        fun setFirstAndLastDay(
            startDate: LocalDate,
            endDate: LocalDate,
        ) {
            _firstDay.value = startDate
            _lastDay.value = endDate
        }
    }
