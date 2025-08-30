package top.goodboyboy.wolfassistant.api.hutapi.message

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MessageAPIService {
    @GET("center/api/v1/instantMessage/appGroupByTag")
    suspend fun getAppGroupByTag(
        @Header("X-Id-Token") accessToken: String,
        @Header("X-Terminal-Info") terminalInfo: String = "app",
    ): ResponseBody

    @GET("center/api/v1/instantMessage/getAppMessageList/new")
    suspend fun getNotice(
        @Header("X-Id-Token") accessToken: String,
        @Header("X-Terminal-Info") terminalInfo: String = "app",
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: Int = 10,
        @Query("appId") appId: String,
    ): ResponseBody
}
