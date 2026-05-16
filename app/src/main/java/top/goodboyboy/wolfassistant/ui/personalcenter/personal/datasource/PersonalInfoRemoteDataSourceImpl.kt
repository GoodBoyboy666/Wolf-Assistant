package top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import okio.IOException
import retrofit2.HttpException
import top.goodboyboy.wolfassistant.api.hutapi.user.UserAPIService
import top.goodboyboy.wolfassistant.api.hutapi.user.UserAvatar
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoRemoteDataSource.DataResult
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.model.PersonalInfo
import javax.inject.Inject

class PersonalInfoRemoteDataSourceImpl
    @Inject
    constructor(
        private val apiService: UserAPIService,
        private val logger: AppLogger,
    ) : PersonalInfoRemoteDataSource {
        override suspend fun getPersonalInfo(accessToken: String): DataResult {
            try {
                logger.tag("PersonalInfoRemote").i("获取个人信息")
                val response =
                    apiService.getUserInfo(accessToken)
                response.use {
                    val infoJsonObject =
                        JsonParser
                            .parseString(it.string())
                            .asJsonObject
                            .get("data")
                            .asJsonObject
                            .get("attributes")
                            .asJsonObject
                    val personalInfo =
                        PersonalInfo(
                            userUid = infoJsonObject.get("userUid").asString,
                            userName = infoJsonObject.get("userName").asString,
                            organizationName = infoJsonObject.get("organizationName").asString,
                            identityTypeName = infoJsonObject.get("identityTypeName").asString,
                            imageUrl =
                                UserAvatar.getUserAvatarUrl(
                                    accessToken = accessToken,
                                    imageUrl = infoJsonObject.get("imageUrl").asString,
                                ),
                        )
                    return DataResult.Success(personalInfo)
                }
            } catch (e: HttpException) {
                logger.e(e, "获取个人信息时发生Http异常")
                return DataResult.Error(
                    Failure.ApiError(
                        e.code(),
                        e.response()?.errorBody()?.string(),
                    ),
                )
            } catch (e: JsonParseException) {
                logger.e(e, "获取个人信息时发生Json解析异常")
                return DataResult.Error(
                    Failure.JsonParsingError(
                        "请求个人信息时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: IOException) {
                logger.e(e, "获取个人信息时发生IO异常")
                return DataResult.Error(Failure.IOError("请求个人信息时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                logger.e(e, "获取个人信息时发生未知异常")
                return DataResult.Error(Failure.UnknownError(e))
            }
        }
    }
