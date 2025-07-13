package top.goodboyboy.hutassistant.ui.messagecenter.datasource

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import okio.IOException
import retrofit2.HttpException
import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.hutapi.message.MessageAPIService
import top.goodboyboy.hutassistant.ui.messagecenter.datasource.MessageDataSource.DataResult
import javax.inject.Inject

class MessageDataSourceImpl
    @Inject
    constructor(
        private val apiService: MessageAPIService,
    ) : MessageDataSource {
        override suspend fun getAppID(accessToken: String): DataResult {
            try {
                val response = apiService.getAppGroupByTag(accessToken)
                response.use {
                    val list =
                        JsonParser
                            .parseString(it.string())
                            .asJsonObject
                            .getAsJsonObject("data")
                            .getAsJsonArray("list")
                    val appid = mutableListOf<String>()
                    list.forEach {
                        appid.add(it.asJsonObject.get("appId").asString)
                    }
                    return DataResult.Success(appid)
                }
            } catch (e: HttpException) {
                return DataResult.Error(
                    Failure.ApiError(
                        e.code(),
                        e.response()?.errorBody()?.string(),
                    ),
                )
            } catch (e: JsonParseException) {
                return DataResult.Error(Failure.JsonParsingError("获取APPID失败！" + e.message, e))
            } catch (e: IOException) {
                return DataResult.Error(Failure.IOError("获取APPID时出现IO错误！" + e.message, e))
            } catch (e: Exception) {
                return DataResult.Error(Failure.UnknownError(e))
            }
        }
    }
