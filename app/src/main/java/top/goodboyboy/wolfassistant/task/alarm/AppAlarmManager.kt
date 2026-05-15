package top.goodboyboy.wolfassistant.task.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAlarmManager
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val alarmManager: AlarmManager,
    ) {
        @SuppressLint("ScheduleExactAlarm")
        fun scheduleWakeUp(
            type: AlarmBizType,
            eventId: Long,
            triggerTime: Long,
        ) {
            val pendingIntent = createPendingIntent(type, eventId, triggerTime)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }

        fun cancelAlarm(eventId: Long) {
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
