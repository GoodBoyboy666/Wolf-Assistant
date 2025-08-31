package top.goodboyboy.wolfassistant.util

import okhttp3.ConnectionSpec
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.TlsVersion
import java.net.Inet4Address
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object UnsafeOkHttpClient {
    fun getUnsafeOkHttpClient(): OkHttpClient =
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )
//            val loggingInterceptor = HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY // 打印请求详情用于调试
//            }

            val permissiveConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .allEnabledCipherSuites()
                .build()
            //解决v6下服务器返回502问题（难蚌，真会折腾人）
            val ipv4OnlyDns = Dns { hostname ->
                Dns.SYSTEM.lookup(hostname).filter { it is Inet4Address }
            }

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
//                .addInterceptor(loggingInterceptor)
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .protocols(listOf(Protocol.HTTP_1_1))
                .hostnameVerifier { _, _ -> true }
                .connectionSpecs(listOf(permissiveConnectionSpec, ConnectionSpec.CLEARTEXT)) // 应用宽容的加密套件
                .dns(ipv4OnlyDns)
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
}
