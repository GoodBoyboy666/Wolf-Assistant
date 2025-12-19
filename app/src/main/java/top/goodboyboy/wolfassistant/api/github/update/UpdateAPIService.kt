package top.goodboyboy.wolfassistant.api.github.update

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface UpdateAPIService {
    @GET("repos/GoodBoyboy666/Wolf-Assistant/releases/latest")
    suspend fun getLatestVersionInfo(): ResponseBody

    @GET("repos/GoodBoyboy666/Wolf-Assistant/releases")
    suspend fun getReleases(
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
    ): ResponseBody
}
