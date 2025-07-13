package top.goodboyboy.hutassistant.ui.login.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.hutassistant.hutapi.user.LoginAPIService
import top.goodboyboy.hutassistant.ui.login.repository.LoginRepository
import top.goodboyboy.hutassistant.ui.login.repository.LoginRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {
    @Provides
    @Singleton
    fun provideLoginRepository(apiService: LoginAPIService): LoginRepository = LoginRepositoryImpl(apiService)
}
