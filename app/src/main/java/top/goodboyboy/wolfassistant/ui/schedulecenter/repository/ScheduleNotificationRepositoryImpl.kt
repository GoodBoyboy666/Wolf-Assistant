package top.goodboyboy.wolfassistant.ui.schedulecenter.repository

import android.content.Context
import android.util.Log
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import top.goodboyboy.wolfassistant.room.const.ScheduleType
import top.goodboyboy.wolfassistant.room.dao.ScheduleNotificationTaskDao
import top.goodboyboy.wolfassistant.room.entity.ScheduleNotificationTaskEntity
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import top.goodboyboy.wolfassistant.util.GsonUtil
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale


class ScheduleNotificationRepositoryImpl(
    private val scheduleNotificationTaskDao: ScheduleNotificationTaskDao,
    private val context: Context,
) : ScheduleNotificationRepository {
    private val baseDir = File(context.filesDir, "schedule")
    override suspend fun setScheduleNotificationTask(time: LocalDate): Boolean {
        val notifications = getScheduleItems(time)
        notifications.forEach { entity ->
            scheduleNotificationTaskDao.insert(
                ScheduleNotificationTaskEntity(
                    title = entity.title,
                    startDate = entity.startDate,
                    endDate = entity.endDate,
                    address = entity.address,
                    remark = entity.remark,
                    triggerTime = entity.startDate.toInstant().toEpochMilli() - 15 * 60 * 1000, // 提前15分钟通知
                    scheduleType = ScheduleType.NORMAL
                )
            )
        }
        return true
    }

    override suspend fun removeScheduleNotificationTask(id: Long): Boolean {
        scheduleNotificationTaskDao.deleteByID(id)
        return true
    }

    override suspend fun removeAllScheduleNotificationTasks(): Boolean {
        scheduleNotificationTaskDao.cleanSchedule(ScheduleType.NORMAL)
        return true
    }

    override suspend fun removeAllLabScheduleNotificationTasks(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun setLabScheduleNotificationTask(week: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getScheduleNotificationTask(id: Long): ScheduleNotificationTaskEntity? {
        scheduleNotificationTaskDao.getByID(id)?.let {
            return it
        }
        return null
    }

    override suspend fun getAllScheduleNotificationTasks(): List<ScheduleNotificationTaskEntity> {
        return scheduleNotificationTaskDao.getAllEntities()
    }

    private suspend fun getScheduleItems(time: LocalDate): List<ScheduleItem> {
        // 获取当前默认系统环境的星期规则
        val weekFields = WeekFields.of(Locale.getDefault())
        // 获取本周的第一天
        val startOfWeek: LocalDate? = time.with(weekFields.dayOfWeek(), 1)
        // 获取本周的最后一天
        val endOfWeek: LocalDate? = time.with(weekFields.dayOfWeek(), 7)
        val scheduleFileName = "${startOfWeek}-${endOfWeek}.json"
        try {
            val scheduleFile = File(baseDir, scheduleFileName)
            if (!scheduleFile.exists()) {
                Log.w(null, "课表缓存文件不存在: ${scheduleFile.absolutePath}")
                return emptyList()
            }
            val fileContent = scheduleFile.readText()
            val type = object : TypeToken<List<ScheduleItem?>>() {}.type
            val scheduleObject =
                GsonUtil.getGson().fromJson<List<ScheduleItem?>>(fileContent, type)
            return scheduleObject.filterNotNull()
        } catch (e: JsonParseException) {
            Log.e(
                null,
                "解析课表时出现Json解析异常" + e.message
            )
        } catch (e: IOException) {
            Log.e(null, "获取课表缓存时出现IO异常" + e.message)
        } catch (e: Exception) {
            Log.e(null, "获取课表缓存时出现未知异常" + e.message)

        }
        return emptyList()
    }

    private fun getLabScheduleItems(week: Int): List<LabScheduleItem> {
        return emptyList()
    }
}
