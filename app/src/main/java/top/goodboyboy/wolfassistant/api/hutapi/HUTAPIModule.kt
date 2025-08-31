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
import top.goodboyboy.wolfassistant.util.UnsafeOkHttpClient.getUnsafeOkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HUTAPIModule {
    @Provides
    @Singleton
    @SafeApi
    fun provideMessageApiService(): MessageAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://message-service.hut.edu.cn/")
            .build()
            .create(MessageAPIService::class.java)

    @Provides
    @Singleton
    @UnsafeApi
    fun provideUnsafeMessageApiService(): MessageAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://message-service.hut.edu.cn/")
            .client(getUnsafeOkHttpClient())
            .build()
            .create(MessageAPIService::class.java)

    @Provides
    @Singleton
    @SafeApi
    fun providePortalAPIService(): PortalAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://portal.hut.edu.cn/")
            .build()
            .create(PortalAPIService::class.java)

    @Provides
    @Singleton
    @UnsafeApi
    fun provideUnsafePortalAPIService(): PortalAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://portal.hut.edu.cn/")
            .client(getUnsafeOkHttpClient())
            .build()
            .create(PortalAPIService::class.java)

    @Provides
    @Singleton
    @SafeApi
    fun provideScheduleAPIService(): ScheduleAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://portal.hut.edu.cn")
            .build()
            .create(ScheduleAPIService::class.java)

    @Provides
    @Singleton
    @UnsafeApi
    fun provideUnsafeScheduleAPIService(): ScheduleAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://portal.hut.edu.cn")
            .client(getUnsafeOkHttpClient())
            .build()
            .create(ScheduleAPIService::class.java)

    @Provides
    @Singleton
    @SafeApi
    fun provideServiceListAPIService(): ServiceListAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://portal.hut.edu.cn/")
            .build()
            .create(ServiceListAPIService::class.java)

    @Provides
    @Singleton
    @UnsafeApi
    fun provideUnsafeServiceListAPIService(): ServiceListAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://portal.hut.edu.cn/")
            .client(getUnsafeOkHttpClient())
            .build()
            .create(ServiceListAPIService::class.java)

    @Provides
    @Singleton
    @SafeApi
    fun provideUserAPIService(): UserAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://authx-service.hut.edu.cn/")
            .build()
            .create(UserAPIService::class.java)

    @Provides
    @Singleton
    @UnsafeApi
    fun provideUnsafeUserAPIService(): UserAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://authx-service.hut.edu.cn/")
            .client(getUnsafeOkHttpClient())
            .build()
            .create(UserAPIService::class.java)

    @Provides
    @Singleton
    @SafeApi
    fun provideLoginAPIService(): LoginAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://mycas.hut.edu.cn/")
            .build()
            .create(LoginAPIService::class.java)

    @Provides
    @Singleton
    @UnsafeApi
    fun provideUnsafeLoginAPIService(): LoginAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://mycas.hut.edu.cn/")
            .client(getUnsafeOkHttpClient())
            .build()
            .create(LoginAPIService::class.java)
}
