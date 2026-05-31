package top.goodboyboy.wolfassistant.ui.messagecenter.datasource

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import okio.IOException
import retrofit2.HttpException
import top.goodboyboy.wolfassistant.api.hutapi.message.MessageAPIService
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.messagecenter.datasource.MessageDataSource.DataResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDataSourceImpl
    @Inject
    constructor(
        private val apiService: MessageAPIService,
        private val logger: AppLogger,
    ) : MessageDataSource {
        override suspend fun getAppID(accessToken: String): DataResult {
            try {
                logger.tag("MessageRemote").i("获取消息应用分组")
                val response =
                    apiService.getAppGroupByTag(accessToken)

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
                logger.e(e, "获取消息应用分组时发生Http异常")
                return DataResult.Error(
                    Failure.ApiError(
                        e.code(),
                        e.response()?.errorBody()?.string(),
                    ),
                )
            } catch (e: JsonParseException) {
                logger.e(e, "获取消息应用分组时发生Json解析异常")
                return DataResult.Error(Failure.JsonParsingError("获取APPID失败！" + e.message, e))
            } catch (e: IOException) {
                logger.e(e, "获取消息应用分组时发生IO异常")
                return DataResult.Error(Failure.IOError("获取APPID时出现IO错误！" + e.message, e))
            } catch (e: Exception) {
                logger.e(e, "获取消息应用分组时发生未知异常")
                return DataResult.Error(Failure.UnknownError(e))
            }
        }
    }
