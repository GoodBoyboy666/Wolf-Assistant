package top.goodboyboy.hutassistant.api.github

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import top.goodboyboy.hutassistant.api.github.update.UpdateAPIService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GithubAPIModule {
    @Provides
    @Singleton
    fun provideUpdateAPIService(): UpdateAPIService =
        Retrofit
            .Builder()
            .baseUrl("https://api.github.com/")
            .build()
            .create(UpdateAPIService::class.java)
}
