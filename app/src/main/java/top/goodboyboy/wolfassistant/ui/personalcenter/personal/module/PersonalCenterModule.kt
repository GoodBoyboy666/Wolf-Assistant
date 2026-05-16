package top.goodboyboy.wolfassistant.ui.personalcenter.personal.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSourceImpl
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoRemoteDataSource
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoRemoteDataSourceImpl
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PersonalCenterModule {
    @Binds
    abstract fun bindPersonalInfoCacheDataSource(impl: PersonalInfoCacheDataSourceImpl): PersonalInfoCacheDataSource

    @Binds
    abstract fun bindPersonalInfoRemoteDataSource(impl: PersonalInfoRemoteDataSourceImpl): PersonalInfoRemoteDataSource

    @Binds
    abstract fun bindPersonalInfoRepository(impl: PersonalInfoRepositoryImpl): PersonalInfoRepository
}
