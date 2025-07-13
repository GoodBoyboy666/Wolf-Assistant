package top.goodboyboy.hutassistant.hutapi.service

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ServiceListAPIService {
    @POST("portal-api/v1/service/list")
    suspend fun getServiceList(
        @Header("X-Id-Token") accessToken: String,
        @Header("X-Terminal-Info") terminalInfo: String = "app",
        @Body body: RequestBody,
    ): ResponseBody
}
