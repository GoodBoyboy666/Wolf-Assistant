package top.goodboyboy.hutassistant.hutapi.schedule

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ScheduleAPIService {
    @GET("portal-api/v1/calendar/share/schedule/getEvents")
    suspend fun getSchedule(
        @Header("X-Id-Token") accessToken: String,
        @Header("X-Terminal-Info") terminalInfo: String = "app",
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): ResponseBody
}
