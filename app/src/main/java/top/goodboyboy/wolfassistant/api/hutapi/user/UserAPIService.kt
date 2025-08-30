package top.goodboyboy.wolfassistant.api.hutapi.user

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header

interface UserAPIService {
    @GET("personal/api/v1/personal/me/user")
    suspend fun getUserInfo(
        @Header("X-Id-Token") accessToken: String,
        @Header("X-Terminal-Info") terminalInfo: String = "app",
    ): ResponseBody
}
