package top.goodboyboy.wolfassistant.notification

import top.goodboyboy.wolfassistant.notification.handler.ScheduleNotificationHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationManager
    @Inject
    constructor(
        private val scheduleNotificationHandler: ScheduleNotificationHandler,
    ) {
        fun dispatch(intent: NotifyIntent) {
            when (intent) {
                is NotifyIntent.ShowScheduleNotification -> scheduleNotificationHandler.send(intent)
            }
        }
    }
