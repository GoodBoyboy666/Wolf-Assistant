package top.goodboyboy.wolfassistant.ui.schedulecenter.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.LabScheduleCacheDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.LabScheduleCacheDataSourceImpl
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.LabScheduleRemoteDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.LabScheduleRemoteDataSourceImpl
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleCacheDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleCacheDataSourceImpl
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSourceImpl
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.LabScheduleRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.LabScheduleRepositoryImpl
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleNotificationRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleNotificationRepositoryImpl
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ScheduleCenterModule {
    @Binds
    abstract fun bindScheduleCacheDataSource(impl: ScheduleCacheDataSourceImpl): ScheduleCacheDataSource

    @Binds
    abstract fun bindScheduleRemoteDataSource(impl: ScheduleRemoteDataSourceImpl): ScheduleRemoteDataSource

    @Binds
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds
    abstract fun bindLabScheduleRemoteDataSource(impl: LabScheduleRemoteDataSourceImpl): LabScheduleRemoteDataSource

    @Binds
    abstract fun bindLabScheduleCacheDataSource(impl: LabScheduleCacheDataSourceImpl): LabScheduleCacheDataSource

    @Binds
    abstract fun bindLabScheduleRepository(impl: LabScheduleRepositoryImpl): LabScheduleRepository

    @Binds
    abstract fun bindScheduleNotificationRepository(impl: ScheduleNotificationRepositoryImpl): ScheduleNotificationRepository
}
