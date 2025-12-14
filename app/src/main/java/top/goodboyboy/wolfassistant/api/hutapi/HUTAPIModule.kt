package top.goodboyboy.wolfassistant.api.hutapi

import android.annotation.SuppressLint
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionSpec
import okhttp3.CookieJar
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.TlsVersion
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
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object HUTAPIModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        val disableSSLCertVerification = GlobalInitConfig.disableSSL
        val onlyIPv4 = GlobalInitConfig.onlyIPv4

        // 禁用SSL配置
        val trustAllCerts =
            arrayOf<TrustManager>(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                    ) {}

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                    ) {}

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                },
            )
//            val loggingInterceptor = HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY // 打印请求详情用于调试
//            }

        val permissiveConnectionSpec =
            ConnectionSpec
                .Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .allEnabledCipherSuites()
                .build()
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        // 解决v6下服务器返回502问题（难蚌，真会折腾人）
        val ipv4OnlyDns =
            Dns { hostname ->
                Dns.SYSTEM.lookup(hostname).filter { it is Inet4Address }
            }

        val client =
            OkHttpClient
                .Builder()
                .cookieJar(cookieJar)

        if (disableSSLCertVerification) {
            client
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .protocols(listOf(Protocol.HTTP_1_1))
                .hostnameVerifier { _, _ -> true }
                .connectionSpecs(listOf(permissiveConnectionSpec, ConnectionSpec.CLEARTEXT)) // 应用宽容的加密套件
        }
        if (onlyIPv4) {
            client.dns(ipv4OnlyDns)
        }
        return client.build()
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
    fun provideLabScheduleOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        val disableSSLCertVerification = GlobalInitConfig.disableSSL
        val onlyIPv4 = GlobalInitConfig.onlyIPv4

        // 禁用SSL配置
        val trustAllCerts =
            arrayOf<TrustManager>(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                },
            )

        val permissiveConnectionSpec =
            ConnectionSpec
                .Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .allEnabledCipherSuites()
                .build()
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        // 禁用IPv6配置
        val ipv4OnlyDns =
            Dns { hostname ->
                Dns.SYSTEM.lookup(hostname).filter { it is Inet4Address }
            }

        // 构建
        val client =
            OkHttpClient
                .Builder()
                .cookieJar(cookieJar)
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
        if (disableSSLCertVerification) {
            client
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .protocols(listOf(Protocol.HTTP_1_1))
                .hostnameVerifier { _, _ -> true }
                .connectionSpecs(listOf(permissiveConnectionSpec, ConnectionSpec.CLEARTEXT)) // 应用宽容的加密套件
        }
        if (onlyIPv4) {
            client.dns(ipv4OnlyDns)
        }
        return client.build()
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
