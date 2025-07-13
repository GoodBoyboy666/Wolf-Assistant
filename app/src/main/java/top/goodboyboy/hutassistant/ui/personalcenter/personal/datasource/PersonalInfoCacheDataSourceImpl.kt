package top.goodboyboy.hutassistant.ui.personalcenter.personal.datasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParseException
import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource.CleanResult
import top.goodboyboy.hutassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource.DataResult
import top.goodboyboy.hutassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource.SaveResult
import top.goodboyboy.hutassistant.ui.personalcenter.personal.model.PersonalInfo
import java.io.File
import java.io.IOException
import javax.inject.Inject

class PersonalInfoCacheDataSourceImpl
    @Inject
    constructor(
        context: Context,
    ) : PersonalInfoCacheDataSource {
        private val personalInfoFile = File(context.filesDir, "personal_info.json")

        override suspend fun getPersonalInfo(): DataResult {
            try {
                if (personalInfoFile.exists() && personalInfoFile.isFile) {
                    val fileContent = personalInfoFile.readText()
                    val cacheObject = Gson().fromJson(fileContent, PersonalInfo::class.java)
                    return DataResult.Success(cacheObject)
                } else {
                    return DataResult.NoCache
                }
            } catch (e: JsonParseException) {
                return DataResult.Error(
                    Failure.JsonParsingError(
                        "获取个人信息时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                return DataResult.Error(Failure.IOError("获取个人信息时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                return DataResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun savePersonalInfo(info: PersonalInfo): SaveResult {
            try {
                val fileContent = Gson().toJson(info)
                personalInfoFile.writeText(fileContent)
                return SaveResult.Success
            } catch (e: IOException) {
                return SaveResult.Error(
                    Failure.IOError(
                        "缓存个人信息时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                return SaveResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun cleanPersonalInfo(): CleanResult {
            try {
                if (personalInfoFile.exists()) {
                    personalInfoFile.delete()
                }
                return CleanResult.Success
            } catch (e: IOException) {
                return CleanResult.Error(
                    Failure.IOError(
                        "清除个人信息时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                return CleanResult.Error(Failure.UnknownError(e))
            }
        }
    }
