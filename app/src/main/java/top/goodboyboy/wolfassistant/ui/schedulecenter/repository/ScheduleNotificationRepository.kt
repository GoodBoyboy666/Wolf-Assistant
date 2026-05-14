package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import top.goodboyboy.wolfassistant.room.entity.ScheduleNotificationTaskEntity
import java.time.LocalDate

interface ScheduleNotificationRepository {
    suspend fun setScheduleNotificationTask(time: LocalDate): Boolean
    suspend fun setLabScheduleNotificationTask(week: Int): Boolean
    suspend fun getScheduleNotificationTask(id: Long): ScheduleNotificationTaskEntity?
    suspend fun getAllScheduleNotificationTasks(): List<ScheduleNotificationTaskEntity>
    suspend fun removeScheduleNotificationTask(id: Long): Boolean
    suspend fun removeAllScheduleNotificationTasks(): Boolean
    suspend fun removeAllLabScheduleNotificationTasks(): Boolean
}
