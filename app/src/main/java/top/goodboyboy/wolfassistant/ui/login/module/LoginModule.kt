package top.goodboyboy.wolfassistant.ui.login.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.ui.login.repository.LoginRepository
import top.goodboyboy.wolfassistant.ui.login.repository.LoginRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginModule {
    @Binds
    abstract fun bindLoginRepository(impl: LoginRepositoryImpl): LoginRepository
}
