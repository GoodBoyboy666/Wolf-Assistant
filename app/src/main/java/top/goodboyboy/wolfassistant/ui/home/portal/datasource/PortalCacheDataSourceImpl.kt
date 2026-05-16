package top.goodboyboy.wolfassistant.ui.home.portal.datasource

import android.content.Context
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortalCacheDataSourceImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
        private val logger: AppLogger,
    ) : PortalCacheDataSource {
        private val baseDir = File(context.filesDir, "portal")

        override suspend fun getPortalCategory(expirationInterval: Int): CacheDataResult<List<PortalCategoryItem>> {
            try {
                logger.tag("PortalCache").i("读取门户分类缓存")
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
                logger.e(e, "读取门户分类缓存时发生IO异常")
                return CacheDataResult.Error(
                    Failure.IOError(
                        "获取缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: JsonParseException) {
                logger.e(e, "读取门户分类缓存时发生Json解析异常")
                return CacheDataResult.Error(
                    Failure.JsonParsingError(
                        "获取缓存时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: SecurityException) {
                logger.e(e, "读取门户分类缓存时发生权限异常")
                return CacheDataResult.Error(
                    Failure.SecurityException(
                        "获取缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                logger.e(e, "读取门户分类缓存时发生未知异常")
                return CacheDataResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun getPortalInfoList(
            portalID: String,
            expirationInterval: Int,
        ): CacheDataResult<List<PortalInfoItem>> {
            try {
                logger.tag("PortalCache").i("读取门户信息列表缓存: portalID=$portalID")
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
                logger.e(e, "读取门户信息列表缓存时发生IO异常")
                return CacheDataResult.Error(
                    Failure.IOError(
                        "获取缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: JsonParseException) {
                logger.e(e, "读取门户信息列表缓存时发生Json解析异常")
                return CacheDataResult.Error(
                    Failure.JsonParsingError(
                        "获取缓存时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: SecurityException) {
                logger.e(e, "读取门户信息列表缓存时发生权限异常")
                return CacheDataResult.Error(
                    Failure.SecurityException(
                        "获取缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                logger.e(e, "读取门户信息列表缓存时发生未知异常")
                return CacheDataResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun savePortalCategory(categories: List<PortalCategoryItem>): SaveResult {
            val categoriesFile = File(baseDir, "categories.json")
            try {
                logger.tag("PortalCache").i("写入门户分类缓存")
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
                logger.e(e, "写入门户分类缓存时发生IO异常")
                return SaveResult.Error(
                    Failure.IOError(
                        "保存缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: JsonParseException) {
                logger.e(e, "写入门户分类缓存时发生Json解析异常")
                return SaveResult.Error(
                    Failure.JsonParsingError(
                        "保存缓存时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: SecurityException) {
                logger.e(e, "写入门户分类缓存时发生权限异常")
                return SaveResult.Error(
                    Failure.SecurityException(
                        "保存缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                logger.e(e, "写入门户分类缓存时发生未知异常")
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
                logger.tag("PortalCache").i("写入门户信息列表缓存: portalID=$portalID")
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
                logger.e(e, "写入门户信息列表缓存时发生IO异常")
                return SaveResult.Error(
                    Failure.IOError(
                        "保存缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: JsonParseException) {
                logger.e(e, "写入门户信息列表缓存时发生Json解析异常")
                return SaveResult.Error(
                    Failure.JsonParsingError(
                        "保存缓存时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: SecurityException) {
                logger.e(e, "写入门户信息列表缓存时发生权限异常")
                return SaveResult.Error(
                    Failure.SecurityException(
                        "保存缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                logger.e(e, "写入门户信息列表缓存时发生未知异常")
                return SaveResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun cleanPortalCategoryCache(): CleanResult {
            val categoriesFile = File(baseDir, "categories.json")
            try {
                logger.tag("PortalCache").i("清除门户分类缓存")
                if (categoriesFile.exists()) {
                    categoriesFile.delete()
                }
                return CleanResult.Success
            } catch (e: SecurityException) {
                logger.e(e, "清除门户分类缓存时发生权限异常")
                return CleanResult.Error(
                    Failure.SecurityException(
                        "清理缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                logger.e(e, "清除门户分类缓存时发生IO异常")
                return CleanResult.Error(
                    Failure.IOError(
                        "清理缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                logger.e(e, "清除门户分类缓存时发生未知异常")
                return CleanResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun cleanPortalInfoCache(): CleanResult {
            val infoDir = File(baseDir, "infos")
            try {
                logger.tag("PortalCache").i("清除门户信息列表缓存")
                infoDir.deleteDirectory()
                return CleanResult.Success
            } catch (e: SecurityException) {
                logger.e(e, "清除门户信息列表缓存时发生权限异常")
                return CleanResult.Error(
                    Failure.SecurityException(
                        "清理缓存时出现权限错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                logger.e(e, "清除门户信息列表缓存时发生IO异常")
                return CleanResult.Error(
                    Failure.IOError(
                        "清理缓存时发生IO错误！" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                logger.e(e, "清除门户信息列表缓存时发生未知异常")
                return CleanResult.Error(Failure.UnknownError(e))
            }
        }
    }
