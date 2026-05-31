package top.goodboyboy.wolfassistant.ui.appsetting.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.ui.appsetting.datasource.GitHubDataSource
import top.goodboyboy.wolfassistant.ui.appsetting.datasource.GitHubDataSourceImpl
import top.goodboyboy.wolfassistant.ui.appsetting.repository.AppSettingRepository
import top.goodboyboy.wolfassistant.ui.appsetting.repository.AppSettingRepositoryImpl
import top.goodboyboy.wolfassistant.ui.appsetting.repository.UpdateRepository
import top.goodboyboy.wolfassistant.ui.appsetting.repository.UpdateRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class AppSettingModule {
    @Binds
    abstract fun bindGitHubDataSource(impl: GitHubDataSourceImpl): GitHubDataSource

    @Binds
    abstract fun bindUpdateRepository(impl: UpdateRepositoryImpl): UpdateRepository

    @Binds
    abstract fun bindAppSettingRepository(impl: AppSettingRepositoryImpl): AppSettingRepository
}
