package top.goodboyboy.wolfassistant.api.github.update

import okhttp3.ResponseBody
import retrofit2.http.GET

interface UpdateAPIService {
    @GET("repos/GoodBoyboy666/Wolf-Assistant/releases/latest")
    suspend fun getLatestVersionInfo(): ResponseBody
}
