package top.goodboyboy.wolfassistant.ui.login.repository

import com.auth0.android.jwt.JWT
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import retrofit2.HttpException
import top.goodboyboy.wolfassistant.api.hutapi.user.LoginAPIService
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.login.model.UserInfo
import top.goodboyboy.wolfassistant.ui.login.repository.LoginRepository.UserData
import javax.inject.Inject

class LoginRepositoryImpl
    @Inject
    constructor(
        private val apiService: LoginAPIService,
    ) : LoginRepository {
        override suspend fun loginUser(
            username: String,
            password: String,
            appId: String,
            deviceId: String,
            osType: String,
            clientId: String,
        ): UserData {
            try {
                val emptyRequestBody =
                    "".toRequestBody("application/x-www-form-urlencoded".toMediaType())
                val response =
                    apiService.loginUser(
                        username,
                        password,
                        appId,
                        deviceId,
                        osType,
                        clientId,
                        emptyRequestBody,
                    )

                response.use {
                    val accessToken =
                        JsonParser
                            .parseString(it.string())
                            .asJsonObject
                            .get("data")
                            .asJsonObject
                            .get(
                                "idToken",
                            ).asString
                    val jwt = JWT(accessToken)
                    val claims = jwt.claims
                    val userId = claims["sub"]?.asString()
                    val userOrganization = claims["ATTR_organizationName"]?.asString()
                    val userName = claims["ATTR_userName"]?.asString()
                    if (userId != null && userOrganization != null && userName != null) {
                        val userInfo =
                            UserInfo(
                                userId,
                                userOrganization,
                                userName,
                                accessToken,
                            )
                        return UserData.Success(userInfo)
                    } else {
                        return UserData.Failed(
                            Failure.JsonParsingError(
                                "解析AccessToken错误！",
                                Throwable(),
                            ),
                        )
                    }
                }
            } catch (e: HttpException) {
                return UserData.Failed(
                    Failure.ApiError(
                        e.code(),
                        e.response()?.errorBody()?.string(),
                    ),
                )
            } catch (e: IOException) {
                return UserData.Failed(Failure.IOError("登录时时出现IO异常" + e.message, e))
            } catch (e: JsonParseException) {
                return UserData.Failed(
                    Failure.JsonParsingError(
                        "登录时出现Json解析异常" + e.message,
                        e,
                    ),
                )
            } catch (e: Exception) {
                return UserData.Failed(Failure.UnknownError(e))
            }
        }
    }
