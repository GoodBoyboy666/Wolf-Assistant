package top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import retrofit2.HttpException
import top.goodboyboy.wolfassistant.api.hutapi.service.ServiceListAPIService
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceRemoteDataSource.DataResult
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.ServiceItem
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.TokenKeyName
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRemoteDataSourceImpl
    @Inject
    constructor(
        private val apiService: ServiceListAPIService,
        private val logger: AppLogger,
    ) : ServiceRemoteDataSource {
        override suspend fun getServiceList(accessToken: String): DataResult {
            try {
                logger.tag("ServiceRemote").i("获取服务列表")
                val emptyRequestBody = "".toRequestBody("application/json".toMediaType())
                val response =
                    apiService.getServiceList(
                        accessToken = accessToken,
                        body = emptyRequestBody,
                    )

                val list = mutableListOf<ServiceItem>()
                response.use {
                    val serviceJsonArray =
                        JsonParser
                            .parseString(it.string())
                            .asJsonObject
                            .get("data")
                            .asJsonArray
                    serviceJsonArray.forEach { array ->
                        val services = array.asJsonObject.get("services").asJsonArray
                        services.forEach { array ->
                            val service = array.asJsonObject
                            val tokenKeyName = parseTokenKeyName(service.get("tokenAccept").asString)
                            val serviceItem =
                                ServiceItem(
                                    service.get("servicePicUrl").asString,
                                    service.get("serviceName").asString,
                                    service.get("serviceUrl").asString,
                                    tokenKeyName,
                                )
                            list.add(serviceItem)
                        }
                    }
                    return DataResult.Success(list.toList())
                }
            } catch (e: HttpException) {
                logger.e(e, "获取服务列表时发生Http异常")
                return DataResult.Error(
                    Failure.ApiError(
                        e.code(),
                        e.response()?.errorBody()?.string(),
                    ),
                )
            } catch (e: IOException) {
                logger.e(e, "获取服务列表时发生IO异常")
                return DataResult.Error(Failure.IOError("请求服务列表时出现IO异常" + e.message, e))
            } catch (e: JsonParseException) {
                logger.e(e, "获取服务列表时发生Json解析异常")
                return DataResult.Error(
                    Failure.JsonParsingError(
                        "请求服务列表时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                logger.e(e, "获取服务列表时发生未知异常")
                return DataResult.Error(Failure.UnknownError(e))
            }
        }

        private fun parseTokenKeyName(typeJson: String): TokenKeyName? {
            var headerTokenKeyName = ""
            var urlTokenKeyName = ""
            if (typeJson != "") {
                val typeJsonObject = JsonParser.parseString(typeJson)
                for (type in typeJsonObject.asJsonArray) {
                    if (type.asJsonObject.get("tokenType").asString == "header") {
                        val header = type.asJsonObject.get("tokenKey").asString
                        if (header.contains('=')) {
                            headerTokenKeyName = header.split('=')[0]
                        } else {
                            headerTokenKeyName = header
                        }
                    } else if (type.asJsonObject.get("tokenType").asString == "url") {
                        val url = type.asJsonObject.get("tokenKey").asString
                        if (url.contains('=')) {
                            urlTokenKeyName = url.split('=')[0]
                        } else {
                            urlTokenKeyName = url
                        }
                    }
                }
            } else {
                return null
            }
            return TokenKeyName(headerTokenKeyName, urlTokenKeyName)
        }
    }
