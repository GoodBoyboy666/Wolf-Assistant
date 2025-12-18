package top.goodboyboy.wolfassistant.api.hutapi.schedule

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface LabScheduleSSOAPIService {
    @FormUrlEncoded
    @POST("cas/login?service=http%3A%2F%2Fjwxt.hut.edu.cn%2Fjsxsd%2Fsso.jsp")
    suspend fun loginToJwxt(
        @Field("username") username: String,
        @Field("password") encryptedPassword: String,
        @Field("captcha") captcha: String = "",
        @Field("mfaState") mfaState: String = "",
        @Field("execution") execution: String,
        @Field("_eventId") eventId: String = "submit",
        @Field("geolocation") geolocation: String = "",
        @Field("submit") submit: String = "Login1",
        @Field("currentMenu") currentMenu: String = "1",
        @Field("failN") failN: String = "0",
    ): Response<ResponseBody>

    @GET("cas/login?service=http%3A%2F%2Fjwxt.hut.edu.cn%2Fjsxsd%2Fsso.jsp")
    suspend fun getLoginPage(): ResponseBody

    @GET("cas/jwt/publicKey")
    suspend fun getPublicKey(): ResponseBody
}
