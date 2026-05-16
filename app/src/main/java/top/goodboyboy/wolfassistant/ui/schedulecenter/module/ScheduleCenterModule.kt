package top.goodboyboy.wolfassistant.ui.schedulecenter.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.hutapi.schedule.LabScheduleAPIService
import top.goodboyboy.wolfassistant.api.hutapi.schedule.LabScheduleSSOAPIService
import top.goodboyboy.wolfassistant.api.hutapi.schedule.ScheduleAPIService
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.room.dao.ScheduleNotificationTaskDao
import top.goodboyboy.wolfassistant.settings.SettingsRepository
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScheduleCenterModule {
    @Provides
    @Singleton
    fun provideScheduleCacheDataSource(
        @ApplicationContext context: Context,
        logger: AppLogger,
    ): ScheduleCacheDataSource = ScheduleCacheDataSourceImpl(context, logger)

    @Provides
    @Singleton
    fun provideScheduleRemoteDataSource(
        apiService: ScheduleAPIService,
        logger: AppLogger,
    ): ScheduleRemoteDataSource = ScheduleRemoteDataSourceImpl(apiService, logger)

    @Provides
    @Singleton
    fun provideScheduleCenterRepository(
        scheduleCacheDataSource: ScheduleCacheDataSource,
        scheduleRemoteDataSource: ScheduleRemoteDataSource,
        logger: AppLogger,
    ): ScheduleRepository = ScheduleRepositoryImpl(scheduleCacheDataSource, scheduleRemoteDataSource, logger)

    @Provides
    @Singleton
    fun provideLabScheduleRemoteDataSource(
        ssoApiService: LabScheduleSSOAPIService,
        scheduleAPIService: LabScheduleAPIService,
        logger: AppLogger,
    ): LabScheduleRemoteDataSource = LabScheduleRemoteDataSourceImpl(ssoApiService, scheduleAPIService, logger)

    @Provides
    @Singleton
    fun provideLabScheduleCacheDataSource(
        @ApplicationContext context: Context,
        logger: AppLogger,
    ): LabScheduleCacheDataSource = LabScheduleCacheDataSourceImpl(context, logger)

    @Provides
    @Singleton
    fun provideLabScheduleRepository(
        labScheduleRemoteDataSource: LabScheduleRemoteDataSource,
        labScheduleCacheDataSource: LabScheduleCacheDataSource,
        settingsRepository: SettingsRepository,
        logger: AppLogger,
    ): LabScheduleRepository =
        LabScheduleRepositoryImpl(labScheduleRemoteDataSource, labScheduleCacheDataSource, settingsRepository, logger)

    @Provides
    @Singleton
    fun provideScheduleNotificationRepository(
        @ApplicationContext context: Context,
        scheduleNotificationTaskDao: ScheduleNotificationTaskDao,
        logger: AppLogger,
    ): ScheduleNotificationRepository = ScheduleNotificationRepositoryImpl(scheduleNotificationTaskDao, context, logger)
}
