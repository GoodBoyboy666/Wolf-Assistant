package top.goodboyboy.wolfassistant.ui.schedulecenter.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.hutapi.schedule.ScheduleAPIService
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleCacheDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleCacheDataSourceImpl
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSource
import top.goodboyboy.wolfassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSourceImpl
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleCenterRepository
import top.goodboyboy.wolfassistant.ui.schedulecenter.repository.ScheduleCenterRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScheduleCenterModule {
    @Provides
    @Singleton
    fun provideScheduleCacheDataSource(
        @ApplicationContext context: Context,
    ): ScheduleCacheDataSource = ScheduleCacheDataSourceImpl(context)

    @Provides
    @Singleton
    fun provideScheduleRemoteDataSource(apiService: ScheduleAPIService): ScheduleRemoteDataSource =
        ScheduleRemoteDataSourceImpl(apiService)

    @Provides
    @Singleton
    fun provideScheduleCenterRepository(
        scheduleCacheDataSource: ScheduleCacheDataSource,
        scheduleRemoteDataSource: ScheduleRemoteDataSource,
    ): ScheduleCenterRepository = ScheduleCenterRepositoryImpl(scheduleCacheDataSource, scheduleRemoteDataSource)
}
