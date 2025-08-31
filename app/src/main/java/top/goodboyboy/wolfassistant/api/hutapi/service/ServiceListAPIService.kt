package top.goodboyboy.wolfassistant.api.hutapi.service

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
        @Header(
            "X-Device-Infos",
        ) deviceInfos: String = "{'packagename':__UNI__AA077AA,'version':1.1.0,'system':Android 16}",
        @Body body: RequestBody,
    ): ResponseBody
}
