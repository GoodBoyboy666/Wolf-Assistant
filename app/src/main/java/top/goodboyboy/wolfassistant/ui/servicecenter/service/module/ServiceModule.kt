package top.goodboyboy.wolfassistant.ui.servicecenter.service.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceCacheDataSource
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceCacheDataSourceImpl
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceRemoteDataSource
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceRemoteDataSourceImpl
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.SearchRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.SearchRepositoryImpl
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    abstract fun bindServiceCacheDataSource(impl: ServiceCacheDataSourceImpl): ServiceCacheDataSource

    @Binds
    abstract fun bindServiceRemoteDataSource(impl: ServiceRemoteDataSourceImpl): ServiceRemoteDataSource

    @Binds
    abstract fun bindServiceRepository(impl: ServiceRepositoryImpl): ServiceRepository

    @Binds
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository
}
