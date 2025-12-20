package top.goodboyboy.wolfassistant.ui.schedulecenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.event.RollBackToCurrentDateEvent
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.LabScheduleRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository.ScheduleData.Failed
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository.ScheduleData.Success
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScheduleCenterViewModel
    @Inject
    constructor(
        private val scheduleRepository: ScheduleRepository,
        private val labScheduleRepository: LabScheduleRepository,
        private val settingsRepository: SettingsRepository,
        globalEventBus: GlobalEventBus,
    ) : ViewModel() {
        companion object {
            const val SCHEDULE_CENTER_TAG = "ScheduleCenter"
        }

        private val _errorMessage = MutableSharedFlow<String>()
        val errorMessage = _errorMessage.asSharedFlow()

        // 普通课表

        private val _loadScheduleState = MutableStateFlow<LoadScheduleState>(LoadScheduleState.Idle)
        val loadScheduleState: StateFlow<LoadScheduleState> = _loadScheduleState.asStateFlow()

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

            data class Failed(
                val message: String,
            ) : LoadScheduleState()
        }

        // 实验课表

        private val _loadLabScheduleState = MutableStateFlow<LoadScheduleState>(LoadScheduleState.Idle)
        val loadLabScheduleState: StateFlow<LoadScheduleState> = _loadLabScheduleState.asStateFlow()

        private val _labScheduleList = MutableStateFlow<List<LabScheduleItem?>>(emptyList())
        val labScheduleList: StateFlow<List<LabScheduleItem?>> = _labScheduleList.asStateFlow()

        private val _weekNumber = MutableStateFlow(1)
        val weekNumber: StateFlow<Int> = _weekNumber.asStateFlow()

        suspend fun loadScheduleList() {
            val startDay = firstDay.value
            val endDay = lastDay.value
            if (startDay == null || endDay == null) {
                _loadScheduleState.value = LoadScheduleState.Failed("日期不可为Null")
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
                scheduleRepository.getSchedule(
                    accessToken,
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
                scheduleRepository.cleanScheduleCache()
            }
        }

        suspend fun cleanLabCache() {
            withContext(Dispatchers.IO) {
                labScheduleRepository.cleanLabScheduleCache()
            }
        }

        fun setFirstAndLastDay(
            startDate: LocalDate,
            endDate: LocalDate,
        ) {
            _firstDay.value = startDate
            _lastDay.value = endDate
        }

        suspend fun loadLabScheduleList() {
            _loadLabScheduleState.value = LoadScheduleState.Loading

            val data = labScheduleRepository.getLabSchedule(weekNumber.first())
            when (data) {
                is LabScheduleRepository.LabScheduleData.Failed -> {
                    _loadLabScheduleState.value =
                        LoadScheduleState.Failed(data.error.message + data.error.cause?.message)
                }

                is LabScheduleRepository.LabScheduleData.Success -> {
                    _labScheduleList.value = data.data
                    _loadLabScheduleState.value = LoadScheduleState.Success
                }
            }
        }

        suspend fun setSelectedWeek(week: Int) {
            settingsRepository.setSelectWeekNum(week)
            _weekNumber.value = week
        }

        init {
            viewModelScope.launch {
                val selectWeek = settingsRepository.selectWeekNum.first()
                _weekNumber.value = selectWeek
            }
        }
    }
