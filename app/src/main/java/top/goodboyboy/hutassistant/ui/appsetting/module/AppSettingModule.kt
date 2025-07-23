package top.goodboyboy.hutassistant.ui.appsetting.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.hutassistant.api.github.update.UpdateAPIService
import top.goodboyboy.hutassistant.ui.appsetting.datasource.GitHubDataSource
import top.goodboyboy.hutassistant.ui.appsetting.datasource.GitHubDataSourceImpl
import top.goodboyboy.hutassistant.ui.appsetting.repository.AppSettingRepository
import top.goodboyboy.hutassistant.ui.appsetting.repository.AppSettingRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppSettingModule {
    @Provides
    @Singleton
    fun provideGitHubDataSource(apiService: UpdateAPIService): GitHubDataSource = GitHubDataSourceImpl(apiService)

    @Provides
    @Singleton
    fun provideAppSettingRepository(gitHubDataSource: GitHubDataSource): AppSettingRepository =
        AppSettingRepositoryImpl(gitHubDataSource)
}
