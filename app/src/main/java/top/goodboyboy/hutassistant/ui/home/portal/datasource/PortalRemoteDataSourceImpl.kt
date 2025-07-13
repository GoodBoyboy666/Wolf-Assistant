package top.goodboyboy.hutassistant.ui.home.portal.datasource

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import retrofit2.HttpException
import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.hutapi.portal.PortalAPIService
import top.goodboyboy.hutassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.hutassistant.ui.home.portal.model.PortalInfoItem
import top.goodboyboy.hutassistant.ui.home.portal.model.RemoteDataResult
import java.io.IOException
import javax.inject.Inject

class PortalRemoteDataSourceImpl
    @Inject
    constructor(
        private val apiService: PortalAPIService,
    ) : PortalRemoteDataSource {
        override suspend fun getPortalCategory(): RemoteDataResult<List<PortalCategoryItem>> {
            try {
                val response = apiService.getPortalCategory()
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
                return RemoteDataResult.Error(Failure.ApiError(e.code(), e.message, e))
            } catch (e: JsonParseException) {
                return RemoteDataResult.Error(
                    Failure.JsonParsingError(
                        "请求时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                return RemoteDataResult.Error(Failure.IOError("请求时发生IO错误！" + e.message, e))
            } catch (e: Exception) {
                return RemoteDataResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun getPortalInfoList(portalID: String): RemoteDataResult<List<PortalInfoItem>> {
            try {
                val response = apiService.getPortalInfo(portalID)

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
                return RemoteDataResult.Error(Failure.ApiError(e.code(), e.message, e))
            } catch (e: JsonParseException) {
                return RemoteDataResult.Error(
                    Failure.JsonParsingError(
                        "请求时出现Json解析错误" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                return RemoteDataResult.Error(Failure.IOError("请求时发生IO错误！" + e.message, e))
            } catch (e: Exception) {
                return RemoteDataResult.Error(Failure.UnknownError(e))
            }
        }
    }
