package top.goodboyboy.wolfassistant.ui.schedulecenter.datasource

import android.content.Context
import com.google.gson.reflect.TypeToken
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem
import top.goodboyboy.wolfassistant.util.GsonUtil
import top.goodboyboy.wolfassistant.util.deleteDirectory
import java.io.File
import java.io.IOException
import javax.inject.Inject

class LabScheduleCacheDataSourceImpl
    @Inject
    constructor(
        context: Context,
    ) : LabScheduleCacheDataSource {
        private val baseDir = File(context.filesDir, "labSchedule")

        override suspend fun getLabScheduleCache(week: Int): LabScheduleCacheDataSource.LabScheduleResult {
            try {
                val labScheduleFile = File(baseDir, "week_$week.json")
                if (labScheduleFile.isFile && labScheduleFile.exists()) {
                    val fileContent = labScheduleFile.readText()
                    val type = object : TypeToken<List<LabScheduleItem?>>() {}.type
                    val scheduleObject =
                        GsonUtil.getGson().fromJson<List<LabScheduleItem?>>(fileContent, type)
                    return LabScheduleCacheDataSource.LabScheduleResult.Success(scheduleObject)
                } else {
                    return LabScheduleCacheDataSource.LabScheduleResult.NoCache
                }
            } catch (e: IOException) {
                return LabScheduleCacheDataSource.LabScheduleResult.Error(
                    Failure.IOError("获取实验课表缓存时出现IO异常" + e.message, e),
                )
            } catch (e: Exception) {
                return LabScheduleCacheDataSource.LabScheduleResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun saveLabScheduleCache(
            data: Map<Int, List<LabScheduleItem?>>,
        ): LabScheduleCacheDataSource.SaveLabScheduleResult {
            try {
                if (!baseDir.exists()) {
                    baseDir.mkdirs()
                }
                for ((week, scheduleList) in data) {
                    val labScheduleFile = File(baseDir, "week_$week.json")
                    val jsonString =
                        GsonUtil.getGson().toJson(
                            scheduleList,
                            object : TypeToken<List<LabScheduleItem?>>() {}.type,
                        )
                    labScheduleFile.writeText(jsonString)
                }
                return LabScheduleCacheDataSource.SaveLabScheduleResult.Success
            } catch (e: IOException) {
                return LabScheduleCacheDataSource.SaveLabScheduleResult.Error(
                    Failure.IOError("保存实验课表缓存时出现IO异常" + e.message, e),
                )
            } catch (e: Exception) {
                return LabScheduleCacheDataSource.SaveLabScheduleResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun cleanLabScheduleCache(): LabScheduleCacheDataSource.CleanLabScheduleResult {
            try {
                baseDir.deleteDirectory()
                return LabScheduleCacheDataSource.CleanLabScheduleResult.Success
            } catch (e: IOException) {
                return LabScheduleCacheDataSource.CleanLabScheduleResult.Error(
                    Failure.IOError("清除实验课表缓存时出现IO异常" + e.message, e),
                )
            } catch (e: Exception) {
                return LabScheduleCacheDataSource.CleanLabScheduleResult.Error(Failure.UnknownError(e))
            }
        }
    }
