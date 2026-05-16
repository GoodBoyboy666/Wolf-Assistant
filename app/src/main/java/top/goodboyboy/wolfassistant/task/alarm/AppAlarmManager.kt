package top.goodboyboy.wolfassistant.task.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import top.goodboyboy.wolfassistant.log.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAlarmManager
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val alarmManager: AlarmManager,
        private val logger: AppLogger,
    ) {
        @SuppressLint("ScheduleExactAlarm")
        fun scheduleWakeUp(
            type: AlarmBizType,
            eventId: Long,
            triggerTime: Long,
        ) {
            logger.tag("Alarm").i("调度闹钟: type=${type.name}, eventId=$eventId")
            val pendingIntent = createPendingIntent(type, eventId, triggerTime)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }

        fun cancelAlarm(eventId: Long) {
            logger.tag("Alarm").i("取消闹钟: eventId=$eventId")
            val pendingIntent = createPendingIntent(AlarmBizType.UNKNOWN, eventId, 0L)
            alarmManager.cancel(pendingIntent)
        }

        private fun createPendingIntent(
            type: AlarmBizType,
            eventId: Long,
            triggerTime: Long,
        ): PendingIntent {
            val intent =
                Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("BIZ_TYPE", type.name)
                    putExtra("EVENT_ID", eventId)
                }

            return PendingIntent.getBroadcast(
                context,
                eventId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
