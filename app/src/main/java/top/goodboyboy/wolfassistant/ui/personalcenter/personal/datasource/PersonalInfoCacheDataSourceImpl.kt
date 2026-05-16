package top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParseException
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource.CleanResult
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource.DataResult
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource.SaveResult
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.model.PersonalInfo
import java.io.File
import java.io.IOException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalInfoCacheDataSourceImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
        private val logger: AppLogger,
    ) : PersonalInfoCacheDataSource {
        private val personalInfoFile = File(context.filesDir, "personal_info.json")

        override suspend fun getPersonalInfo(): DataResult {
            try {
                logger.tag("PersonalInfoCache").i("读取个人信息缓存")
                if (personalInfoFile.exists() && personalInfoFile.isFile) {
                    val fileContent = personalInfoFile.readText()
                    val cacheObject = Gson().fromJson(fileContent, PersonalInfo::class.java)
                    return DataResult.Success(cacheObject)
                } else {
                    return DataResult.NoCache
                }
            } catch (e: JsonParseException) {
                logger.e(e, "读取个人信息缓存时发生Json解析异常")
                return DataResult.Error(
                    Failure.JsonParsingError(
                        "获取个人信息时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                logger.e(e, "读取个人信息缓存时发生IO异常")
                return DataResult.Error(Failure.IOError("获取个人信息时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                logger.e(e, "读取个人信息缓存时发生未知异常")
                return DataResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun savePersonalInfo(info: PersonalInfo): SaveResult {
            try {
                logger.tag("PersonalInfoCache").i("写入个人信息缓存")
                val fileContent = Gson().toJson(info)
                personalInfoFile.writeText(fileContent)
                return SaveResult.Success
            } catch (e: IOException) {
                logger.e(e, "写入个人信息缓存时发生IO异常")
                return SaveResult.Error(
                    Failure.IOError(
                        "缓存个人信息时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                logger.e(e, "写入个人信息缓存时发生未知异常")
                return SaveResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun cleanPersonalInfo(): CleanResult {
            try {
                logger.tag("PersonalInfoCache").i("清除个人信息缓存")
                if (personalInfoFile.exists()) {
                    personalInfoFile.delete()
                }
                return CleanResult.Success
            } catch (e: IOException) {
                logger.e(e, "清除个人信息缓存时发生IO异常")
                return CleanResult.Error(
                    Failure.IOError(
                        "清除个人信息时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                logger.e(e, "清除个人信息缓存时发生未知异常")
                return CleanResult.Error(Failure.UnknownError(e))
            }
        }
    }
