package top.goodboyboy.wolfassistant.ui.home.portal.datasource

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import retrofit2.HttpException
import top.goodboyboy.wolfassistant.api.hutapi.portal.PortalAPIService
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalInfoItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.RemoteDataResult
import java.io.IOException
import javax.inject.Inject

class PortalRemoteDataSourceImpl
    @Inject
    constructor(
        private val apiService: PortalAPIService,
        private val logger: AppLogger,
    ) : PortalRemoteDataSource {
        override suspend fun getPortalCategory(accessToken: String): RemoteDataResult<List<PortalCategoryItem>> {
            try {
                logger.tag("PortalRemote").i("获取门户分类")
                val response =
                    apiService.getPortalCategory(
                        accessToken = accessToken,
                    )
                response.use {
                    val portalCategory =
                        JsonParser
                            .parseString(it.string())
                            .asJsonObject
                            .get("data")
                            .asJsonArray
                    val list = mutableListOf<PortalCategoryItem>()
                    portalCategory.forEach { category ->
                        val portalCategoryObject = category.asJsonObject
                        val item =
                            PortalCategoryItem(
                                portalID = portalCategoryObject.get("id").asString,
                                portalName = portalCategoryObject.get("columnName").asString,
                            )
                        list.add(item)
                    }
                    return RemoteDataResult.Success(list)
                }
            } catch (e: HttpException) {
                logger.e(e, "获取门户分类时发生Http异常")
                return RemoteDataResult.Error(Failure.ApiError(e.code(), e.message, e))
            } catch (e: JsonParseException) {
                logger.e(e, "获取门户分类时发生Json解析异常")
                return RemoteDataResult.Error(
                    Failure.JsonParsingError(
                        "请求时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                logger.e(e, "获取门户分类时发生IO异常")
                return RemoteDataResult.Error(Failure.IOError("请求时发生IO错误！" + e.message, e))
            } catch (e: Exception) {
                logger.e(e, "获取门户分类时发生未知异常")
                return RemoteDataResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun getPortalInfoList(portalID: String): RemoteDataResult<List<PortalInfoItem>> {
            try {
                logger.tag("PortalRemote").i("获取门户信息列表: portalID=$portalID")
                val response =
                    apiService.getPortalInfo(portalID)

                response.use {
                    val portalInfos =
                        JsonParser
                            .parseString(it.string())
                            .asJsonObject
                            .get("data")
                            .asJsonObject
                            .get(
                                "allContents",
                            ).asJsonArray
                    val list = mutableListOf<PortalInfoItem>()
                    portalInfos.forEach { info ->
                        val infoObject = info.asJsonObject
                        val portalInfoItem =
                            PortalInfoItem(
                                title = infoObject.get("title").asString,
                                author = infoObject.get("releaseDeptName").asString,
                                createTime = infoObject.get("releaseStartTime").asString,
                                url = infoObject.get("url").asString,
                            )
                        list.add(portalInfoItem)
                    }
                    return RemoteDataResult.Success(list)
                }
            } catch (e: HttpException) {
                logger.e(e, "获取门户信息列表时发生Http异常")
                return RemoteDataResult.Error(Failure.ApiError(e.code(), e.message, e))
            } catch (e: JsonParseException) {
                logger.e(e, "获取门户信息列表时发生Json解析异常")
                return RemoteDataResult.Error(
                    Failure.JsonParsingError(
                        "请求时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                logger.e(e, "获取门户信息列表时发生IO异常")
                return RemoteDataResult.Error(Failure.IOError("请求时发生IO错误！" + e.message, e))
            } catch (e: Exception) {
                logger.e(e, "获取门户信息列表时发生未知异常")
                return RemoteDataResult.Error(Failure.UnknownError(e))
            }
        }
    }
