package top.goodboyboy.wolfassistant.ui.home.portal.datasource

import android.content.Context
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalCacheDataSource.CleanResult
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalCacheDataSource.SaveResult
import top.goodboyboy.wolfassistant.ui.home.portal.model.CacheDataResult
import top.goodboyboy.wolfassistant.ui.home.portal.model.CacheItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalInfoItem
import top.goodboyboy.wolfassistant.util.GsonUtil.getGson
import top.goodboyboy.wolfassistant.util.deleteDirectory
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class PortalCacheDataSourceImpl
    @Inject
    constructor(
        context: Context,
    ) : PortalCacheDataSource {
        private val baseDir = File(context.filesDir, "portal")

        override suspend fun getPortalCategory(expirationInterval: Int): CacheDataResult<List<PortalCategoryItem>> {
            try {
                if (baseDir.exists()) {
                    val categoriesFile = File(baseDir, "categories.json")
                    if (categoriesFile.exists()) {
                        val fileContent = categoriesFile.readText()
                        val typeToken =
                            object : TypeToken<CacheItem<List<PortalCategoryItem>>>() {}.type
                        val cacheObject =
                            getGson().fromJson<CacheItem<List<PortalCategoryItem>>>(
                                fileContent,
                                typeToken,
                            )
                        if (Duration
                                .between(cacheObject.createTime, LocalDateTime.now())
                                .toHours() < expirationInterval
                        ) {
                            return CacheDataResult.Success(cacheObject.cacheObject)
                        }
                    }
                }
                return CacheDataResult.NoCache
            } catch (e: IOException) {
                return CacheDataResult.Error(
                    Failure.IOError(
                        "获取缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: JsonParseException) {
                return CacheDataResult.Error(
                    Failure.JsonParsingError(
                        "获取缓存时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: SecurityException) {
                return CacheDataResult.Error(
                    Failure.SecurityException(
                        "获取缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                return CacheDataResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun getPortalInfoList(
            portalID: String,
            expirationInterval: Int,
        ): CacheDataResult<List<PortalInfoItem>> {
            try {
                val infoDir = File(baseDir, "infos")
                if (infoDir.exists()) {
                    val infoFile = File(infoDir, "$portalID.json")
                    if (infoFile.exists()) {
                        val fileContent = infoFile.readText()
                        val typeToken = object : TypeToken<CacheItem<List<PortalInfoItem>>>() {}.type
                        val cacheObject =
                            getGson().fromJson<CacheItem<List<PortalInfoItem>>>(fileContent, typeToken)
                        if (Duration
                                .between(cacheObject.createTime, LocalDateTime.now())
                                .toHours() < expirationInterval
                        ) {
                            return CacheDataResult.Success(cacheObject.cacheObject)
                        }
                    }
                }
                return CacheDataResult.NoCache
            } catch (e: IOException) {
                return CacheDataResult.Error(
                    Failure.IOError(
                        "获取缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: JsonParseException) {
                return CacheDataResult.Error(
                    Failure.JsonParsingError(
                        "获取缓存时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: SecurityException) {
                return CacheDataResult.Error(
                    Failure.SecurityException(
                        "获取缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                return CacheDataResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun savePortalCategory(categories: List<PortalCategoryItem>): SaveResult {
            val categoriesFile = File(baseDir, "categories.json")
            try {
                categoriesFile.parentFile?.mkdirs()
                val cacheItem =
                    CacheItem(
                        LocalDateTime.now(),
                        categories,
                    )
                val categoriesJsonText = getGson().toJson(cacheItem)
                categoriesFile.writeText(categoriesJsonText)
                return SaveResult.Success
            } catch (e: IOException) {
                return SaveResult.Error(
                    Failure.IOError(
                        "保存缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: JsonParseException) {
                return SaveResult.Error(
                    Failure.JsonParsingError(
                        "保存缓存时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: SecurityException) {
                return SaveResult.Error(
                    Failure.SecurityException(
                        "保存缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                return SaveResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun savePortalInfoList(
            portalID: String,
            infos: List<PortalInfoItem>,
        ): SaveResult {
            val infoDir = File(baseDir, "infos")
            val infoFile = File(infoDir, "$portalID.json")
            try {
                infoFile.parentFile?.mkdirs()
                val cacheItem =
                    CacheItem(
                        LocalDateTime.now(),
                        infos,
                    )
                val infoJsonText = getGson().toJson(cacheItem)
                infoFile.writeText(infoJsonText)
                return SaveResult.Success
            } catch (e: IOException) {
                return SaveResult.Error(
                    Failure.IOError(
                        "保存缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: JsonParseException) {
                return SaveResult.Error(
                    Failure.JsonParsingError(
                        "保存缓存时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: SecurityException) {
                return SaveResult.Error(
                    Failure.SecurityException(
                        "保存缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                return SaveResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun cleanPortalCategoryCache(): CleanResult {
            val categoriesFile = File(baseDir, "categories.json")
            try {
                if (categoriesFile.exists()) {
                    categoriesFile.delete()
                }
                return CleanResult.Success
            } catch (e: SecurityException) {
                return CleanResult.Error(
                    Failure.SecurityException(
                        "清理缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                return CleanResult.Error(
                    Failure.IOError(
                        "清理缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                return CleanResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun cleanPortalInfoCache(): CleanResult {
            val infoDir = File(baseDir, "infos")
            try {
                infoDir.deleteDirectory()
                return CleanResult.Success
            } catch (e: SecurityException) {
                return CleanResult.Error(
                    Failure.SecurityException(
                        "清理缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                return CleanResult.Error(
                    Failure.IOError(
                        "清理缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                return CleanResult.Error(Failure.UnknownError(e))
            }
        }
    }
