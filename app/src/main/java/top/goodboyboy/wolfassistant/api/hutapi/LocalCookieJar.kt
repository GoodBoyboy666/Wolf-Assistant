package top.goodboyboy.wolfassistant.api.hutapi

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class LocalCookieJar : CookieJar {
    private val cookieStore = mutableMapOf<String, List<Cookie>>()

    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>,
    ) {
        cookieStore[url.host] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore[url.host] ?: ArrayList()
}
