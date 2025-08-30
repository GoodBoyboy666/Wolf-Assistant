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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HUTAPIModule {
    @Provides
    @Singleton
    fun provideMessageApiService(): MessageAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://message-service.hut.edu.cn/")
            .build()
            .create(MessageAPIService::class.java)

    @Provides
    @Singleton
    fun providePortalAPIService(): PortalAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://portal.hut.edu.cn/")
            .build()
            .create(PortalAPIService::class.java)

    @Provides
    @Singleton
    fun provideScheduleAPIService(): ScheduleAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://portal.hut.edu.cn")
            .build()
            .create(ScheduleAPIService::class.java)

    @Provides
    @Singleton
    fun provideServiceListAPIService(): ServiceListAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://portal.hut.edu.cn/")
            .build()
            .create(ServiceListAPIService::class.java)

    @Provides
    @Singleton
    fun provideUserAPIService(): UserAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://authx-service.hut.edu.cn/")
            .build()
            .create(UserAPIService::class.java)

    @Provides
    @Singleton
    fun provideLoginAPIService(): LoginAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://mycas.hut.edu.cn/")
            .build()
            .create(LoginAPIService::class.java)
}
