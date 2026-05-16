package top.goodboyboy.wolfassistant.ui.schedulecenter.datasource

import android.content.Context
import com.google.gson.reflect.TypeToken
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
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
        private val logger: AppLogger,
    ) : LabScheduleCacheDataSource {
        private val baseDir = File(context.filesDir, "labSchedule")

        override suspend fun getLabScheduleCache(week: Int): LabScheduleCacheDataSource.LabScheduleResult {
            try {
                logger.tag("LabScheduleCache").i("读取实验课表缓存: 第${week}周")
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
                logger.e(e, "读取实验课表缓存时发生IO异常")
                return LabScheduleCacheDataSource.LabScheduleResult.Error(
                    Failure.IOError("获取实验课表缓存时出现IO异常" + e.message, e),
                )
            } catch (e: Exception) {
                logger.e(e, "读取实验课表缓存时发生未知异常")
                return LabScheduleCacheDataSource.LabScheduleResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun saveLabScheduleCache(
            data: Map<Int, List<LabScheduleItem?>>,
        ): LabScheduleCacheDataSource.SaveLabScheduleResult {
            try {
                logger.tag("LabScheduleCache").i("写入实验课表缓存: ${data.keys}")
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
                logger.e(e, "写入实验课表缓存时发生IO异常")
                return LabScheduleCacheDataSource.SaveLabScheduleResult.Error(
                    Failure.IOError("保存实验课表缓存时出现IO异常" + e.message, e),
                )
            } catch (e: Exception) {
                logger.e(e, "写入实验课表缓存时发生未知异常")
                return LabScheduleCacheDataSource.SaveLabScheduleResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun cleanLabScheduleCache(): LabScheduleCacheDataSource.CleanLabScheduleResult {
            try {
                logger.tag("LabScheduleCache").i("清除实验课表缓存")
                baseDir.deleteDirectory()
                return LabScheduleCacheDataSource.CleanLabScheduleResult.Success
            } catch (e: IOException) {
                logger.e(e, "清除实验课表缓存时发生IO异常")
                return LabScheduleCacheDataSource.CleanLabScheduleResult.Error(
                    Failure.IOError("清除实验课表缓存时出现IO异常" + e.message, e),
                )
            } catch (e: Exception) {
                logger.e(e, "清除实验课表缓存时发生未知异常")
                return LabScheduleCacheDataSource.CleanLabScheduleResult.Error(Failure.UnknownError(e))
            }
        }
    }
