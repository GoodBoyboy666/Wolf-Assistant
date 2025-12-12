package top.goodboyboy.wolfassistant.api.hutapi

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import top.goodboyboy.wolfassistant.api.hutapi.message.MessageAPIService
import top.goodboyboy.wolfassistant.api.hutapi.portal.PortalAPIService
import top.goodboyboy.wolfassistant.api.hutapi.schedule.LabScheduleAPIService
import top.goodboyboy.wolfassistant.api.hutapi.schedule.LabScheduleSSOAPIService
import top.goodboyboy.wolfassistant.api.hutapi.schedule.ScheduleAPIService
import top.goodboyboy.wolfassistant.api.hutapi.service.ServiceListAPIService
import top.goodboyboy.wolfassistant.api.hutapi.user.LoginAPIService
import top.goodboyboy.wolfassistant.api.hutapi.user.UserAPIService
import top.goodboyboy.wolfassistant.ui.appsetting.GlobalInitConfig
import top.goodboyboy.wolfassistant.util.UnsafeOkHttpClient.getUnsafeOkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HUTAPIModule {
    @Provides
    @Singleton
    fun provideMessageApiService(): MessageAPIService {
        val disableSSLCertVerification = GlobalInitConfig.disableSSL
        val onlyIPv4 = GlobalInitConfig.onlyIPv4
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://message-service.hut.edu.cn/")
        if (disableSSLCertVerification) {
            builder
                .client(getUnsafeOkHttpClient(onlyIPv4))
        }
        return builder.build().create(MessageAPIService::class.java)
    }

    @Provides
    @Singleton
    fun providePortalAPIService(): PortalAPIService {
        val disableSSLCertVerification = GlobalInitConfig.disableSSL
        val onlyIPv4 = GlobalInitConfig.onlyIPv4
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://portal.hut.edu.cn/")
        if (disableSSLCertVerification) {
            builder
                .client(getUnsafeOkHttpClient(onlyIPv4))
        }
        return builder
            .build()
            .create(PortalAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideScheduleAPIService(): ScheduleAPIService {
        val disableSSLCertVerification = GlobalInitConfig.disableSSL
        val onlyIPv4 = GlobalInitConfig.onlyIPv4
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://portal.hut.edu.cn")
        if (disableSSLCertVerification) {
            builder
                .client(getUnsafeOkHttpClient(onlyIPv4))
        }
        return builder
            .build()
            .create(ScheduleAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideServiceListAPIService(): ServiceListAPIService {
        val disableSSLCertVerification = GlobalInitConfig.disableSSL
        val onlyIPv4 = GlobalInitConfig.onlyIPv4
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://portal.hut.edu.cn/")
        if (disableSSLCertVerification) {
            builder
                .client(getUnsafeOkHttpClient(onlyIPv4))
        }
        return builder
            .build()
            .create(ServiceListAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserAPIService(): UserAPIService {
        val disableSSLCertVerification = GlobalInitConfig.disableSSL
        val onlyIPv4 = GlobalInitConfig.onlyIPv4
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://authx-service.hut.edu.cn/")

        if (disableSSLCertVerification) {
            builder
                .client(getUnsafeOkHttpClient(onlyIPv4))
        }
        return builder
            .build()
            .create(UserAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideLoginAPIService(): LoginAPIService {
        val disableSSLCertVerification = GlobalInitConfig.disableSSL
        val onlyIPv4 = GlobalInitConfig.onlyIPv4
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://mycas.hut.edu.cn/")
        if (disableSSLCertVerification) {
            builder
                .client(getUnsafeOkHttpClient(onlyIPv4))
        }
        return builder
            .build()
            .create(LoginAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar = LocalCookieJar()

    @Provides
    @Singleton
    fun provideLabScheduleOkHttpClient(cookieJar: CookieJar): OkHttpClient =
        OkHttpClient
            .Builder()
            .cookieJar(cookieJar)
            .followRedirects(true)
            .addNetworkInterceptor { chain ->
                val original = chain.request()
                val requestBuilder =
                    original
                        .newBuilder()
                        // 1. UA 和 Accept 保持你现在的设置（非常完美）
                        .header(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0",
                        ).header(
                            "Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
                        ).header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Connection", "keep-alive")
                        // 2. 【新增】浏览器发了，你也得发
                        .header("Cache-Control", "no-cache")
                        .header("Pragma", "no-cache")
                        .header("DNT", "1") // Do Not Track

                // 3. 【新增】手动伪造 Referer
                // 虽然浏览器从 HTTPS 跳到 HTTP 会丢弃 Referer，但手动补上通常能修复很多弱智后端 Bug
                // 只有当请求的是 jwxt 时才加，避免污染其他请求
                if (original.url.host.contains("jwxt")) {
                    requestBuilder.header("Referer", "https://mycas.hut.edu.cn/")
                }

                chain.proceed(requestBuilder.build())
            }
//            .addNetworkInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
            .build()

    @Provides
    @Singleton
    fun provideLabScheduleSSOAPIService(client: OkHttpClient): LabScheduleSSOAPIService {
        val disableSSLCertVerification = GlobalInitConfig.disableSSL
        val onlyIPv4 = GlobalInitConfig.onlyIPv4
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://mycas.hut.edu.cn/")
                .client(client)
        return builder
            .build()
            .create(LabScheduleSSOAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideLabScheduleAPIService(client: OkHttpClient): LabScheduleAPIService {
        val disableSSLCertVerification = GlobalInitConfig.disableSSL
        val onlyIPv4 = GlobalInitConfig.onlyIPv4
        val builder =
            Retrofit
                .Builder()
                .baseUrl("http://jwxt.hut.edu.cn/")
                .client(client)
        return builder
            .build()
            .create(LabScheduleAPIService::class.java)
    }
}
