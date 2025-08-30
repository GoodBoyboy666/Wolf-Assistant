package top.goodboyboy.wolfassistant.ui.schedulecenter.datasource

import android.content.Context
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleCacheDataSource.CleanResult
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleCacheDataSource.DataResult
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleCacheDataSource.SaveResult
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.ScheduleItem
import top.goodboyboy.wolfassistant.util.GsonUtil
import top.goodboyboy.wolfassistant.util.deleteDirectory
import java.io.File
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

class ScheduleCacheDataSourceImpl
    @Inject
    constructor(
        context: Context,
    ) : ScheduleCacheDataSource {
        private val baseDir = File(context.filesDir, "schedule")

        override suspend fun getSchedule(
            startDate: LocalDate,
            endDate: LocalDate,
        ): DataResult {
            try {
                val scheduleFile = File(baseDir, "$startDate-$endDate.json")
                if (scheduleFile.isFile && scheduleFile.exists()) {
                    val fileContent = scheduleFile.readText()
                    val type = object : TypeToken<List<ScheduleItem?>>() {}.type
                    val scheduleObject =
                        GsonUtil.getGson().fromJson<List<ScheduleItem?>>(fileContent, type)
                    return DataResult.Success(scheduleObject)
                } else {
                    return DataResult.NoCache
                }
            } catch (e: JsonParseException) {
                return DataResult.Error(
                    Failure.JsonParsingError(
                        "获取课表缓存时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                return DataResult.Error(Failure.IOError("获取课表缓存时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                return DataResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun saveSchedule(
            startDate: LocalDate,
            endDate: LocalDate,
            list: List<ScheduleItem?>,
        ): SaveResult {
            try {
                val scheduleFile = File(baseDir, "$startDate-$endDate.json")
                scheduleFile.parentFile?.mkdirs()
                val scheduleContent = GsonUtil.getGson().toJson(list)
                scheduleFile.writeText(scheduleContent)
                return SaveResult.Success
            } catch (e: IOException) {
                return SaveResult.Error(Failure.IOError("保存课表缓存时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                return SaveResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun cleanSchedule(): CleanResult {
            try {
                baseDir.deleteDirectory()
                return CleanResult.Success
            } catch (e: IOException) {
                return CleanResult.Error(Failure.IOError("清除课表缓存时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                return CleanResult.Error(Failure.UnknownError(e))
            }
        }
    }
