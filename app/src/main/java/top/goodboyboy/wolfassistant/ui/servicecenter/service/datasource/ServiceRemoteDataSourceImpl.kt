package top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import retrofit2.HttpException
import top.goodboyboy.wolfassistant.api.hutapi.SafeApi
import top.goodboyboy.wolfassistant.api.hutapi.UnsafeApi
import top.goodboyboy.wolfassistant.api.hutapi.service.ServiceListAPIService
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceRemoteDataSource.DataResult
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.ServiceItem
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.TokenKeyName
import javax.inject.Inject

class ServiceRemoteDataSourceImpl
    @Inject
    constructor(
        @param:SafeApi private val apiService: ServiceListAPIService,
        @param:UnsafeApi private val unsafeAPIService: ServiceListAPIService,
    ) : ServiceRemoteDataSource {
        override suspend fun getServiceList(
            accessToken: String,
            disableSSLCertVerification: Boolean,
        ): DataResult {
            try {
                val emptyRequestBody = "".toRequestBody("application/json".toMediaType())
                val response =
                    if (disableSSLCertVerification) {
                        unsafeAPIService.getServiceList(
                            accessToken = accessToken,
                            body = emptyRequestBody,
                        )
                    } else {
                        apiService.getServiceList(
                            accessToken = accessToken,
                            body = emptyRequestBody,
                        )
                    }
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
                return DataResult.Error(
                    Failure.ApiError(
                        e.code(),
                        e.response()?.errorBody()?.string(),
                    ),
                )
            } catch (e: IOException) {
                return DataResult.Error(Failure.IOError("请求服务列表时出现IO异常" + e.message, e))
            } catch (e: JsonParseException) {
                return DataResult.Error(
                    Failure.JsonParsingError(
                        "请求服务列表时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
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
