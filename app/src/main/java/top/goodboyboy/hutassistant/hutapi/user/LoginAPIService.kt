package top.goodboyboy.hutassistant.hutapi.user

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginAPIService {
    @POST("token/password/passwordLogin")
    suspend fun loginUser(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("appId") appId: String,
        @Query("deviceId") deviceId: String,
        @Query("osType") osType: String,
        @Query("clientId") clientId: String,
        @Body body: RequestBody,
    ): ResponseBody
}
