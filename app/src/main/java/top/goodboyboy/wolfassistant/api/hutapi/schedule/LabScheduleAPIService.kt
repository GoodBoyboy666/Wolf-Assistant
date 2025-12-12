package top.goodboyboy.wolfassistant.api.hutapi.schedule

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface LabScheduleAPIService {
    @GET("jsxsd/sso.jsp")
    suspend fun getPreFlightPage(): Response<ResponseBody>

    @GET("jsxsd/syjx/toXskb.do")
    suspend fun getLabSchedule(): ResponseBody
}
