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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale


class ScheduleNotificationRepositoryImpl(
    private val scheduleNotificationTaskDao: ScheduleNotificationTaskDao,
    private val context: Context,
) : ScheduleNotificationRepository {

    override suspend fun setScheduleNotificationTask(time: LocalDate) {
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
    }

    override suspend fun removeScheduleNotificationTask(id: Long) {
        scheduleNotificationTaskDao.deleteByID(id)
    }

    override suspend fun removeAllScheduleNotificationTasks() {
        scheduleNotificationTaskDao.cleanSchedule(ScheduleType.NORMAL)
    }

    override suspend fun removeAllLabScheduleNotificationTasks() {
        scheduleNotificationTaskDao.cleanSchedule(ScheduleType.LAB)
    }

    override suspend fun setLabScheduleNotificationTask(week: Int) {
        val notifications = getLabScheduleItems(week)
        notifications.forEachIndexed { index, item ->
            if (item != null) {
                val day = index % 7
                val jieci = index / 7
                val now = LocalDateTime.now()
                val time = when (jieci) {
                    0 -> LocalTime.of(8, 0)
                    1 -> LocalTime.of(10, 0)
                    2 -> LocalTime.of(14, 0)
                    3 -> LocalTime.of(16, 0)
                    4 -> LocalTime.of(19, 0)
                    else -> LocalTime.of(21, 0)
                }
                val startDayTime = now
                    .with(DayOfWeek.of(day + 1))
                    .with(time)
                val startDayTimeMillis = startDayTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val endDayTime = startDayTime.plusHours(1).plusMinutes(30)
                scheduleNotificationTaskDao.insert(
                    ScheduleNotificationTaskEntity(
                        title = item.courseName,
                        startDate = startDayTime.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
                        endDate = endDayTime.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
                        address = item.location,
                        remark = "",
                        triggerTime = startDayTimeMillis - 15 * 60 * 1000, // 提前15分钟通知
                        scheduleType = ScheduleType.LAB
                    )
                )
            }
        }
    }

    override suspend fun getScheduleNotificationTask(id: Long): ScheduleNotificationTaskEntity? {
        scheduleNotificationTaskDao.getByID(id)?.let {
            return it
        }
        return null
    }

    override suspend fun getAllScheduleNotificationTasks(): List<ScheduleNotificationTaskEntity> {
        return scheduleNotificationTaskDao.getAllEntitiesByType(ScheduleType.NORMAL)
    }

    override suspend fun getAllLabScheduleNotificationTasks(): List<ScheduleNotificationTaskEntity> {
        return scheduleNotificationTaskDao.getAllEntitiesByType(ScheduleType.LAB)
    }

    private suspend fun getScheduleItems(time: LocalDate): List<ScheduleItem> {
        val baseDir = File(context.filesDir, "schedule")
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

    private suspend fun getLabScheduleItems(week: Int): List<LabScheduleItem?> {
        val baseDir = File(context.filesDir, "labSchedule")
        try {
            val labScheduleFile = File(baseDir, "week_$week.json")
            if (labScheduleFile.isFile && labScheduleFile.exists()) {
                val fileContent = labScheduleFile.readText()
                val type = object : TypeToken<List<LabScheduleItem?>>() {}.type
                val scheduleObject =
                    GsonUtil.getGson().fromJson<List<LabScheduleItem?>>(fileContent, type)
                return scheduleObject
            }
        } catch (e: JsonParseException) {
            Log.e(
                null,
                "解析实验课表时出现Json解析异常" + e.message
            )
        } catch (e: IOException) {
            Log.e(null, "获取实验课表缓存时出现IO异常" + e.message)
        } catch (e: Exception) {
            Log.e(null, "获取实验课表缓存时出现未知异常" + e.message)
        }
        return emptyList()
    }
}
