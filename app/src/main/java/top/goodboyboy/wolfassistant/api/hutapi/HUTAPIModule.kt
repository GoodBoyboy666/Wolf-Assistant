package top.goodboyboy.wolfassistant.api.hutapi

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import top.goodboyboy.wolfassistant.api.hutapi.message.MessageAPIService
import top.goodboyboy.wolfassistant.api.hutapi.portal.PortalAPIService
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
}
