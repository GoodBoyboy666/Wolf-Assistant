package top.goodboyboy.hutassistant.hutapi.user

import androidx.core.net.toUri
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object UserAvatar {
    fun getUserAvatar(
        accessToken: String,
        imageUrl: String,
    ): Response {
        val requestUrl = getUserAvatarUrl(accessToken, imageUrl)
        val request =
            Request
                .Builder()
                .url(requestUrl)
                .get()
                .build()
        val client = OkHttpClient()
        return client.newCall(request).execute()
    }

    fun getUserAvatarUrl(
        accessToken: String,
        imageUrl: String,
    ): String {
        val url = "https://authx-service.hut.edu.cn/personal/api/v1/personal/me/portrait"
        val uri = url.toUri()
        val builder = uri.buildUpon()
        builder.appendQueryParameter("idToken", accessToken)
        builder.appendQueryParameter("imageUrl", imageUrl)
        return builder.build().toString()
    }
}
