package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import top.goodboyboy.wolfassistant.room.entity.ScheduleNotificationTaskEntity
import java.time.LocalDate

interface ScheduleNotificationRepository {
    suspend fun setScheduleNotificationTask(time: LocalDate)
    suspend fun setLabScheduleNotificationTask(week: Int)
    suspend fun getScheduleNotificationTask(id: Long): ScheduleNotificationTaskEntity?
    suspend fun getAllScheduleNotificationTasks(): List<ScheduleNotificationTaskEntity>
    suspend fun getAllLabScheduleNotificationTasks(): List<ScheduleNotificationTaskEntity>
    suspend fun removeScheduleNotificationTask(id: Long)
    suspend fun removeAllScheduleNotificationTasks()
    suspend fun removeAllLabScheduleNotificationTasks()
}
