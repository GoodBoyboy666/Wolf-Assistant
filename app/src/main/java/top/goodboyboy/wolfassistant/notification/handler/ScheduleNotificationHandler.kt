package top.goodboyboy.wolfassistant.notification.handler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import top.goodboyboy.wolfassistant.R
import top.goodboyboy.wolfassistant.notification.NotifyIntent
import javax.inject.Inject

class ScheduleNotificationHandler @Inject constructor(
    private val notificationManager: NotificationManager,
    @param:ApplicationContext private val context: Context
) {
    fun send(intent: NotifyIntent.ShowScheduleNotification) {
        val channelId = "schedule_notification_channel"
        val notificationId = intent.id

        val channel = NotificationChannel(
            channelId,
            "上课提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "用于上课提醒的通知渠道"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)

        // 构建通知
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(intent.title)
            .setContentText(intent.content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setGroup("SCHEDULE_NOTIFICATIONS")
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }
}
