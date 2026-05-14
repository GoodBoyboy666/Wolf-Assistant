package top.goodboyboy.wolfassistant.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import top.goodboyboy.wolfassistant.room.const.ScheduleType
import java.time.OffsetDateTime

@Entity(tableName = "schedule_notification_tasks")
data class ScheduleNotificationTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startDate: OffsetDateTime,
    val endDate: OffsetDateTime,
    val address: String,
    val remark: String,
    val triggerTime: Long,
    val scheduleType: ScheduleType,
)
