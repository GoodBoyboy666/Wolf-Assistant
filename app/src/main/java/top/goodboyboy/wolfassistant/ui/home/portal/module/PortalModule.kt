package top.goodboyboy.wolfassistant.ui.home.portal.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.hutapi.SafeApi
import top.goodboyboy.wolfassistant.api.hutapi.UnsafeApi
import top.goodboyboy.wolfassistant.api.hutapi.portal.PortalAPIService
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalCacheDataSource
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalCacheDataSourceImpl
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalRemoteDataSource
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalRemoteDataSourceImpl
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PortalModule {
    @Provides
    @Singleton
    fun providePortalRemoteDataSource(
        @SafeApi apiService: PortalAPIService,
        @UnsafeApi unsafeAPIService: PortalAPIService,
    ): PortalRemoteDataSource = PortalRemoteDataSourceImpl(apiService, unsafeAPIService)

    @Provides
    @Singleton
    fun providePortalCacheDataSource(
        @ApplicationContext context: Context,
    ): PortalCacheDataSource = PortalCacheDataSourceImpl(context)

    @Provides
    @Singleton
    fun providePortalRepository(
        remoteDataSource: PortalRemoteDataSource,
        cacheDataSource: PortalCacheDataSource,
        settingsRepository: SettingsRepository,
    ): PortalRepository = PortalRepositoryImpl(remoteDataSource, cacheDataSource, settingsRepository)
}
