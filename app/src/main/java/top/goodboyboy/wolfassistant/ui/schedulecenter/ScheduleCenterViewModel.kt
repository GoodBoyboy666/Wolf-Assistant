package top.goodboyboy.wolfassistant.ui.schedulecenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.event.RollBackToCurrentDateEvent
import top.goodboyboy.wolfassistant.ui.schedulecenter.event.SubmitScheduleNotification
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
        private val scheduleNotificationController: ScheduleNotificationController,
        globalEventBus: GlobalEventBus,
        private val logger: AppLogger,
    ) : ViewModel() {
        companion object {
            const val SCHEDULE_CENTER_TAG = "ScheduleCenter"
        }

        private val _errorMessage = Channel<String>(Channel.BUFFERED)
        val errorMessage = _errorMessage.receiveAsFlow()

        // 订阅课表提示框展示

        private val _showScheduleNotificationDialog = MutableStateFlow(false)
        val showScheduleNotificationDialog: StateFlow<Boolean> = _showScheduleNotificationDialog.asStateFlow()

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

            object Failed : LoadScheduleState()
        }

        // 实验课表

        private val _loadLabScheduleState = MutableStateFlow<LoadScheduleState>(LoadScheduleState.Idle)
        val loadLabScheduleState: StateFlow<LoadScheduleState> = _loadLabScheduleState.asStateFlow()

        private val _labScheduleList = MutableStateFlow<List<LabScheduleItem?>>(emptyList())
        val labScheduleList: StateFlow<List<LabScheduleItem?>> = _labScheduleList.asStateFlow()

        private val _weekNumber = MutableStateFlow(1)
        val weekNumber: StateFlow<Int> = _weekNumber.asStateFlow()

        suspend fun loadScheduleList(forceRefresh: Boolean = false) {
            logger.i("加载课表")
            val startDay = firstDay.value
            val endDay = lastDay.value
            if (startDay == null || endDay == null) {
                logger.e(null, "日期不可为Null")
                _errorMessage.send("日期不可为Null")
                _loadScheduleState.value = LoadScheduleState.Failed
            } else {
                loadSchedule(startDay, endDay, forceRefresh)
            }
        }

        private suspend fun loadSchedule(
            startDate: LocalDate,
            endDate: LocalDate,
            forceRefresh: Boolean = false,
        ) {
            _loadScheduleState.value = LoadScheduleState.Loading
            val accessToken = settingsRepository.getAccessTokenDecrypted()
            val data =
                scheduleRepository.getSchedule(
                    accessToken,
                    startDate,
                    endDate,
                    forceRefresh,
                )
            when (data) {
                is Failed -> {
                    _errorMessage.send(data.error.message)
                    _loadScheduleState.value = LoadScheduleState.Failed
                    logger.e(data.error.cause, "加载课表失败")
                }

                is Success -> {
                    logger.i("加载课表成功")
                    _scheduleList.value = data.data
                    _loadScheduleState.value = LoadScheduleState.Success
                }
            }
        }

        suspend fun cleanCache() {
            logger.i("清除课表缓存")
            withContext(Dispatchers.IO) {
                scheduleRepository.cleanScheduleCache()
            }
        }

        suspend fun cleanLabCache() {
            logger.i("清除课表缓存")
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

        suspend fun loadLabScheduleList(forceRefresh: Boolean = false) {
            logger.i("加载实验课表")
            _loadLabScheduleState.value = LoadScheduleState.Loading

            val data = labScheduleRepository.getLabSchedule(weekNumber.first(), forceRefresh)
            when (data) {
                is LabScheduleRepository.LabScheduleData.Failed -> {
                    _errorMessage.send(data.error.message + data.error.cause?.message)
                    logger.e(data.error.cause, "加载实验课表失败")
                    _loadLabScheduleState.value = LoadScheduleState.Failed
                }

                is LabScheduleRepository.LabScheduleData.Success -> {
                    logger.i("加载实验课表成功")
                    _labScheduleList.value = data.data
                    _loadLabScheduleState.value = LoadScheduleState.Success
                }
            }
        }

        suspend fun setSelectedWeek(week: Int) {
            settingsRepository.setSelectWeekNum(week)
            _weekNumber.value = week
        }

        suspend fun setScheduleNotification() {
            logger.i("设置课表通知")
            if (loadScheduleState.value != LoadScheduleState.Success ||
                loadLabScheduleState.value != LoadScheduleState.Success
            ) {
                logger.e(null, "请先成功加载所有课表后再设置课表通知（包括实验课表）")
                _errorMessage.send("请先成功加载所有课表后再设置课表通知（包括实验课表）")
                return
            } else {
                withContext(Dispatchers.IO) {
                    // 先取消所有的课表通知，再设置新的课表通知
                    // 理论课表
                    scheduleNotificationController.cancelScheduleNotificationAlarms()
                    scheduleNotificationController.addScheduleNotificationTask(firstDay.value ?: LocalDate.now())
                    scheduleNotificationController.setScheduleNotificationAlarm()

                    // 实验课表
                    scheduleNotificationController.cancelLabScheduleNotificationAlarms()
                    scheduleNotificationController.addLabScheduleNotificationTask(weekNumber.value)
                    scheduleNotificationController.setLabScheduleNotificationAlarm()
                }
            }
        }

        fun setShowScheduleNotificationDialog(show: Boolean) {
            _showScheduleNotificationDialog.value = show
        }

        init {
            viewModelScope.launch {
                val selectWeek = settingsRepository.selectWeekNum.first()
                _weekNumber.value = selectWeek
                globalEventBus
                    .subscribeToTarget<SubmitScheduleNotification>(
                        SCHEDULE_CENTER_TAG,
                    ).collect {
                        _showScheduleNotificationDialog.value = true
                    }
            }
        }
    }
