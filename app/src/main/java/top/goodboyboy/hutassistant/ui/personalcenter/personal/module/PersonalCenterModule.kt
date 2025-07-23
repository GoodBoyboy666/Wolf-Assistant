package top.goodboyboy.hutassistant.ui.personalcenter.personal.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.hutassistant.api.hutapi.user.UserAPIService
import top.goodboyboy.hutassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource
import top.goodboyboy.hutassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSourceImpl
import top.goodboyboy.hutassistant.ui.personalcenter.personal.datasource.PersonalInfoRemoteDataSource
import top.goodboyboy.hutassistant.ui.personalcenter.personal.datasource.PersonalInfoRemoteDataSourceImpl
import top.goodboyboy.hutassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import top.goodboyboy.hutassistant.ui.personalcenter.personal.repository.PersonalInfoRepositoryImpl
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
    fun providePersonalInfoRemoteDataSource(apiService: UserAPIService): PersonalInfoRemoteDataSource =
        PersonalInfoRemoteDataSourceImpl(apiService)

    @Provides
    @Singleton
    fun providePersonalInfoRepository(
        cacheDataSource: PersonalInfoCacheDataSource,
        remoteDataSource: PersonalInfoRemoteDataSource,
    ): PersonalInfoRepository = PersonalInfoRepositoryImpl(cacheDataSource, remoteDataSource)
}
