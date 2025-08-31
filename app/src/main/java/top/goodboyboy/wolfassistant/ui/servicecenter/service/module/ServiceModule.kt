package top.goodboyboy.wolfassistant.ui.servicecenter.service.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.hutapi.SafeApi
import top.goodboyboy.wolfassistant.api.hutapi.UnsafeApi
import top.goodboyboy.wolfassistant.api.hutapi.service.ServiceListAPIService
import top.goodboyboy.wolfassistant.room.dao.ServiceItemDao
import top.goodboyboy.wolfassistant.room.dao.TokenKeyNameDao
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceCacheDataSource
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceCacheDataSourceImpl
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceRemoteDataSource
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceRemoteDataSourceImpl
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideServiceCacheDataSource(
        serviceItemDao: ServiceItemDao,
        tokenKeyNameDao: TokenKeyNameDao,
    ): ServiceCacheDataSource = ServiceCacheDataSourceImpl(serviceItemDao, tokenKeyNameDao)

    @Provides
    @Singleton
    fun provideServiceRemoteDataSource(
        @SafeApi apiService: ServiceListAPIService,
        @UnsafeApi unsafeAPIService: ServiceListAPIService,
        settingsRepository: SettingsRepository,
    ): ServiceRemoteDataSource = ServiceRemoteDataSourceImpl(apiService, unsafeAPIService, settingsRepository)

    @Provides
    @Singleton
    fun provideServiceRepository(
        cacheDataSource: ServiceCacheDataSource,
        remoteDataSource: ServiceRemoteDataSource,
    ): ServiceRepository = ServiceRepositoryImpl(cacheDataSource, remoteDataSource)
}
