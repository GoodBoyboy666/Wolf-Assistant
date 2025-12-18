package top.goodboyboy.wolfassistant.api.hutapi

import android.annotation.SuppressLint
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.CookieJar
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.internal.tls.OkHostnameVerifier
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
import java.net.Inet4Address
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object HUTAPIModule {
    @Provides
    @Singleton
    fun provideSharedConnectionPool(): ConnectionPool = ConnectionPool(5, 1, TimeUnit.MINUTES)

    @Provides
    @Singleton
    fun provideOkHttpClient(okHttpBuilder: OkHttpClient.Builder): OkHttpClient = okHttpBuilder.build()

    @Provides
    fun provideOKHttpBuilder(
        cookieJar: CookieJar,
        sharedPool: ConnectionPool,
    ): OkHttpClient.Builder {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val systemDefaultTrustManager =
            trustManagerFactory.trustManagers.first {
                it is X509TrustManager
            } as X509TrustManager

        val dynamicTrustManager =
            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?,
                ) {
                    systemDefaultTrustManager.checkClientTrusted(chain, authType)
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?,
                ) {
                    // 判断是否禁用SSL验证
                    if (GlobalInitConfig.disableSSL) {
                        Log.w("SSL_Warning", "Ignoring server certificate validation")
                        return
                    } else {
                        Log.i("SSL_Info", "执行系统标准校验")
                        systemDefaultTrustManager.checkServerTrusted(chain, authType)
                    }
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = systemDefaultTrustManager.acceptedIssuers
            }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(dynamicTrustManager), SecureRandom())

        val dynamicHostnameVerifier =
            HostnameVerifier { hostname, session ->
                // 判断是否禁用主机名验证
                if (GlobalInitConfig.disableSSL) {
                    true
                } else {
                    OkHostnameVerifier.verify(hostname, session)
                }
            }

        // 解决v6下服务器返回502问题（难蚌，真会折腾人）
        val dynamicIpv4Only =
            Dns { hostname ->
                if (GlobalInitConfig.onlyIPv4) {
                    Dns.SYSTEM.lookup(hostname).filter { it is Inet4Address }
                } else {
                    Dns.SYSTEM.lookup(hostname)
                }
            }
        val builder =
            OkHttpClient
                .Builder()
                .cookieJar(cookieJar)
                .connectionPool(sharedPool)
                .sslSocketFactory(sslContext.socketFactory, dynamicTrustManager)
                .hostnameVerifier(dynamicHostnameVerifier)
                .dns(dynamicIpv4Only)
        return builder
    }

    @Provides
    @Singleton
    fun provideMessageApiService(client: OkHttpClient): MessageAPIService {
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://message-service.hut.edu.cn/")
                .client(client)

        return builder.build().create(MessageAPIService::class.java)
    }

    @Provides
    @Singleton
    fun providePortalAPIService(client: OkHttpClient): PortalAPIService {
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://portal.hut.edu.cn/")
                .client(client)

        return builder
            .build()
            .create(PortalAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideScheduleAPIService(client: OkHttpClient): ScheduleAPIService {
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://portal.hut.edu.cn")
                .client(client)

        return builder
            .build()
            .create(ScheduleAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideServiceListAPIService(client: OkHttpClient): ServiceListAPIService {
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://portal.hut.edu.cn/")
                .client(client)

        return builder
            .build()
            .create(ServiceListAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserAPIService(client: OkHttpClient): UserAPIService {
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://authx-service.hut.edu.cn/")
                .client(client)

        return builder
            .build()
            .create(UserAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideLoginAPIService(client: OkHttpClient): LoginAPIService {
        val builder =
            Retrofit
                .Builder()
                .baseUrl("https://mycas.hut.edu.cn/")
                .client(client)

        return builder
            .build()
            .create(LoginAPIService::class.java)
    }

    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar = LocalCookieJar()

    @Provides
    @Singleton
    @Named("LabSchedule")
    fun provideLabScheduleOkHttpClient(okHttpBuilder: OkHttpClient.Builder): OkHttpClient {
        // 构建
        okHttpBuilder
            .followRedirects(true)
            .addNetworkInterceptor { chain ->
                val original = chain.request()
                val requestBuilder =
                    original
                        .newBuilder()
                        .header(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0",
                        ).header(
                            "Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
                        ).header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Connection", "keep-alive")
                        .header("Cache-Control", "no-cache")
                        .header("Pragma", "no-cache")
                        .header("DNT", "1")

                // 手动伪造 Referer
                // 虽然浏览器从 HTTPS 跳到 HTTP 会丢弃 Referer，但手动补上通常能修复很多弱智后端 Bug
                // 只有当请求的是 jwxt 时才加，避免污染其他请求
                if (original.url.host.contains("jwxt")) {
                    requestBuilder.header("Referer", "https://mycas.hut.edu.cn/")
                }

                chain.proceed(requestBuilder.build())
            }
//            .addNetworkInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
        return okHttpBuilder.build()
    }

    @Provides
    @Singleton
    fun provideLabScheduleSSOAPIService(
        @Named("LabSchedule") client: OkHttpClient,
    ): LabScheduleSSOAPIService {
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
    fun provideLabScheduleAPIService(
        @Named("LabSchedule") client: OkHttpClient,
    ): LabScheduleAPIService {
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
