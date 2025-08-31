package top.goodboyboy.wolfassistant.ui.login.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.hutapi.SafeApi
import top.goodboyboy.wolfassistant.api.hutapi.UnsafeApi
import top.goodboyboy.wolfassistant.api.hutapi.user.LoginAPIService
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.login.repository.LoginRepository
import top.goodboyboy.wolfassistant.ui.login.repository.LoginRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {
    @Provides
    @Singleton
    fun provideLoginRepository(
        @SafeApi apiService: LoginAPIService,
        @UnsafeApi unsafeApiService: LoginAPIService,
        settingsRepository: SettingsRepository,
    ): LoginRepository = LoginRepositoryImpl(apiService, unsafeApiService, settingsRepository)
}
