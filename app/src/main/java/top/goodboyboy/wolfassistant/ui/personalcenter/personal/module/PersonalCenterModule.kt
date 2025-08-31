package top.goodboyboy.wolfassistant.ui.personalcenter.personal.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.hutapi.SafeApi
import top.goodboyboy.wolfassistant.api.hutapi.UnsafeApi
import top.goodboyboy.wolfassistant.api.hutapi.user.UserAPIService
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSourceImpl
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoRemoteDataSource
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoRemoteDataSourceImpl
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersonalCenterModule {
    @Provides
    @Singleton
    fun providePersonalInfoCacheDataSource(
        @ApplicationContext context: Context,
    ): PersonalInfoCacheDataSource = PersonalInfoCacheDataSourceImpl(context)

    @Provides
    @Singleton
    fun providePersonalInfoRemoteDataSource(
        @SafeApi apiService: UserAPIService,
        @UnsafeApi unsafeAPIService: UserAPIService,
        settingsRepository: SettingsRepository,
    ): PersonalInfoRemoteDataSource = PersonalInfoRemoteDataSourceImpl(apiService, unsafeAPIService, settingsRepository)

    @Provides
    @Singleton
    fun providePersonalInfoRepository(
        cacheDataSource: PersonalInfoCacheDataSource,
        remoteDataSource: PersonalInfoRemoteDataSource,
    ): PersonalInfoRepository = PersonalInfoRepositoryImpl(cacheDataSource, remoteDataSource)
}
