package top.goodboyboy.wolfassistant.api.hutapi.schedule

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ScheduleAPIService {
    @GET("portal-api/v1/calendar/share/schedule/getEvents")
    suspend fun getSchedule(
        @Header("X-Id-Token") accessToken: String,
        @Header("X-Terminal-Info") terminalInfo: String = "app",
        @Header(
            "X-Device-Infos",
        ) deviceInfos: String = "{'packagename':__UNI__AA077AA,'version':1.1.0,'system':Android 16}",
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): ResponseBody
}
