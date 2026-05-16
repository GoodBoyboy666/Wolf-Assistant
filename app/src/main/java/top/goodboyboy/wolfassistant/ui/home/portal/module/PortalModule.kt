package top.goodboyboy.wolfassistant.ui.home.portal.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.hutapi.portal.PortalAPIService
import top.goodboyboy.wolfassistant.log.AppLogger
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
        apiService: PortalAPIService,
        logger: AppLogger,
    ): PortalRemoteDataSource = PortalRemoteDataSourceImpl(apiService, logger)

    @Provides
    @Singleton
    fun providePortalCacheDataSource(
        @ApplicationContext context: Context,
        logger: AppLogger,
    ): PortalCacheDataSource = PortalCacheDataSourceImpl(context, logger)

    @Provides
    @Singleton
    fun providePortalRepository(
        remoteDataSource: PortalRemoteDataSource,
        cacheDataSource: PortalCacheDataSource,
        logger: AppLogger,
    ): PortalRepository = PortalRepositoryImpl(remoteDataSource, cacheDataSource, logger)
}
