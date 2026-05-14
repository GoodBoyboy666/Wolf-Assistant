package top.goodboyboy.wolfassistant.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import top.goodboyboy.wolfassistant.room.const.ScheduleType
import top.goodboyboy.wolfassistant.room.entity.ScheduleNotificationTaskEntity

@Dao
interface ScheduleNotificationTaskDao {
    @Insert
    suspend fun insert(task: ScheduleNotificationTaskEntity): Long

    @Delete
    suspend fun delete(task: ScheduleNotificationTaskEntity)

    @Query("DELETE FROM schedule_notification_tasks WHERE id = :id")
    suspend fun deleteByID(id: Long)

    @Query("DELETE FROM schedule_notification_tasks WHERE scheduleType = :type")
    suspend fun cleanSchedule(type: ScheduleType)

    @Update
    suspend fun update(task: ScheduleNotificationTaskEntity)

    @Query("SELECT * FROM schedule_notification_tasks WHERE id = :id")
    suspend fun getByID(id: Long): ScheduleNotificationTaskEntity?

    @Query("SELECT * FROM schedule_notification_tasks")
    suspend fun getAllEntities(): List<ScheduleNotificationTaskEntity>
}
