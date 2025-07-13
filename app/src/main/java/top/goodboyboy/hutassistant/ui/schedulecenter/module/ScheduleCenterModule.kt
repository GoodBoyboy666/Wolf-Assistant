package top.goodboyboy.hutassistant.ui.schedulecenter.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.hutassistant.hutapi.schedule.ScheduleAPIService
import top.goodboyboy.hutassistant.ui.schedulecenter.datasource.ScheduleCacheDataSource
import top.goodboyboy.hutassistant.ui.schedulecenter.datasource.ScheduleCacheDataSourceImpl
import top.goodboyboy.hutassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSource
import top.goodboyboy.hutassistant.ui.schedulecenter.datasource.ScheduleRemoteDataSourceImpl
import top.goodboyboy.hutassistant.ui.schedulecenter.repository.ScheduleCenterRepository
import top.goodboyboy.hutassistant.ui.schedulecenter.repository.ScheduleCenterRepositoryImpl
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
