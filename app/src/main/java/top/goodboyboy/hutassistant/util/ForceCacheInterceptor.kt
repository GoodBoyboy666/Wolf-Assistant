package top.goodboyboy.hutassistant.util

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ForceCacheInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse: Response = chain.proceed(chain.request())
        // 7天缓存
        val maxAge = 60 * 60 * 24 * 7

        return originalResponse
            .newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", "public, max-age=$maxAge")
            .build()
    }
}
