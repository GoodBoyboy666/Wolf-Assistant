package top.goodboyboy.wolfassistant.ui.schedulecenter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import top.goodboyboy.wolfassistant.common.AlarmTriggeredEvent
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.di.AppModule
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.notification.AppNotificationManager
import top.goodboyboy.wolfassistant.notification.NotifyIntent
import top.goodboyboy.wolfassistant.task.alarm.AlarmBizType
import top.goodboyboy.wolfassistant.task.alarm.AppAlarmManager
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleNotificationRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleNotificationController
    @Inject
    constructor(
        private val eventBus: GlobalEventBus,
        private val scheduleNotificationRepository: ScheduleNotificationRepository,
        private val appAlarmManager: AppAlarmManager,
        private val appNotificationManager: AppNotificationManager,
        @param:AppModule.ApplicationScope private val scope: CoroutineScope,
        private val logger: AppLogger,
    ) {
        init {
            eventBus
                .subscribeToTarget<AlarmTriggeredEvent>(
                    AlarmBizType.SCHEDULE_REMINDER.name,
                ).onEach { event ->
                    val notificationData = scheduleNotificationRepository.getScheduleNotificationTask(event.eventId)
                    if (notificationData != null) {
                        appNotificationManager.dispatch(
                            NotifyIntent.ShowScheduleNotification(
                                id = notificationData.id.toInt(),
                                title = notificationData.title,
                                content = "接下来的课程是${notificationData.title}，将在${
                                    notificationData.startDate.atZoneSameInstant(
                                        ZoneId.systemDefault(),
                                    ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                }开始，地点为${notificationData.address}",
                            ),
                        )
                    }
                }.launchIn(scope)
        }

        suspend fun addScheduleNotificationTask(time: LocalDate) {
            logger.tag("ScheduleNotify").i("添加课表通知任务: $time")
            scheduleNotificationRepository.setScheduleNotificationTask(time)
        }

        suspend fun setScheduleNotificationAlarm() {
            val tasks = scheduleNotificationRepository.getAllScheduleNotificationTasks()
            logger.tag("ScheduleNotify").i("设置课表通知闹钟，共${tasks.size}个任务")
            tasks.forEach { task ->
                if (System.currentTimeMillis() < task.triggerTime) {
                    appAlarmManager.scheduleWakeUp(
                        AlarmBizType.SCHEDULE_REMINDER,
                        task.id,
                        task.triggerTime,
                    )
                }
            }
        }

        suspend fun addLabScheduleNotificationTask(week: Int) {
            logger.tag("ScheduleNotify").i("添加实验课表通知任务: 第${week}周")
            scheduleNotificationRepository.setLabScheduleNotificationTask(week)
        }

        suspend fun setLabScheduleNotificationAlarm() {
            val tasks = scheduleNotificationRepository.getAllLabScheduleNotificationTasks()
            logger.tag("ScheduleNotify").i("设置实验课表通知闹钟，共${tasks.size}个任务")
            tasks.forEach { task ->
                if (System.currentTimeMillis() < task.triggerTime) {
                    appAlarmManager.scheduleWakeUp(
                        AlarmBizType.SCHEDULE_REMINDER,
                        task.id,
                        task.triggerTime,
                    )
                }
            }
        }

        suspend fun cancelScheduleNotificationAlarms() {
            logger.tag("ScheduleNotify").i("取消全部课表通知闹钟")
            val tasks = scheduleNotificationRepository.getAllScheduleNotificationTasks()
            tasks.forEach { task ->
                appAlarmManager.cancelAlarm(task.id)
            }
            scheduleNotificationRepository.removeAllScheduleNotificationTasks()
        }

        suspend fun cancelLabScheduleNotificationAlarms() {
            logger.tag("ScheduleNotify").i("取消全部实验课表通知闹钟")
            val tasks = scheduleNotificationRepository.getAllLabScheduleNotificationTasks()
            tasks.forEach { task ->
                appAlarmManager.cancelAlarm(task.id)
            }
            scheduleNotificationRepository.removeAllLabScheduleNotificationTasks()
        }
    }
