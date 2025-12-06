package top.goodboyboy.wolfassistant.ui.appsetting.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.github.update.UpdateAPIService
import top.goodboyboy.wolfassistant.ui.appsetting.datasource.GitHubDataSource
import top.goodboyboy.wolfassistant.ui.appsetting.datasource.GitHubDataSourceImpl
import top.goodboyboy.wolfassistant.ui.appsetting.repository.AppSettingRepository
import top.goodboyboy.wolfassistant.ui.appsetting.repository.AppSettingRepositoryImpl
import top.goodboyboy.wolfassistant.ui.appsetting.util.VersionUpdateChecker
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppSettingModule {
    @Provides
    @Singleton
    fun provideGitHubDataSource(apiService: UpdateAPIService): GitHubDataSource = GitHubDataSourceImpl(apiService)

    @Provides
    @Singleton
    fun provideAppSettingRepository(versionUpdateChecker: VersionUpdateChecker): AppSettingRepository =
        AppSettingRepositoryImpl(versionUpdateChecker)

    @Provides
    @Singleton
    fun provideVersionUpdateChecker(gitHubDataSource: GitHubDataSource) = VersionUpdateChecker(gitHubDataSource)
}
