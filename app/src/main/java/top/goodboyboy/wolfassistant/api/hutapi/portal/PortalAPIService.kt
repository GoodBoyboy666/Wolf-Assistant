package top.goodboyboy.wolfassistant.api.hutapi.portal

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface PortalAPIService {
    @GET("portal-api/v1/cms/Column/getColumnList")
    suspend fun getPortalCategory(): ResponseBody

    @GET("portal-api/v3/cms/content/getColumncontents")
    suspend fun getPortalInfo(
        @Query("columnId") portalId: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("pageSize") pageSize: Int = 5,
        @Query("loadContent") loadContent: Boolean = false,
        @Query("loadPicContents") loadPicContents: Boolean = false,
    ): ResponseBody
}
