package top.goodboyboy.wolfassistant.ui.home.portal.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalCacheDataSource
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalCacheDataSourceImpl
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalRemoteDataSource
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalRemoteDataSourceImpl
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PortalModule {
    @Binds
    abstract fun bindPortalRemoteDataSource(impl: PortalRemoteDataSourceImpl): PortalRemoteDataSource

    @Binds
    abstract fun bindPortalCacheDataSource(impl: PortalCacheDataSourceImpl): PortalCacheDataSource

    @Binds
    abstract fun bindPortalRepository(impl: PortalRepositoryImpl): PortalRepository
}
