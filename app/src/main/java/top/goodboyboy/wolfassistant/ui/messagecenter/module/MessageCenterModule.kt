package top.goodboyboy.wolfassistant.ui.messagecenter.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.hutapi.SafeApi
import top.goodboyboy.wolfassistant.api.hutapi.UnsafeApi
import top.goodboyboy.wolfassistant.api.hutapi.message.MessageAPIService
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.messagecenter.datasource.MessageDataSource
import top.goodboyboy.wolfassistant.ui.messagecenter.datasource.MessageDataSourceImpl
import top.goodboyboy.wolfassistant.ui.messagecenter.repository.MessageRepository
import top.goodboyboy.wolfassistant.ui.messagecenter.repository.MessageRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MessageCenterModule {
    @Provides
    @Singleton
    fun provideMessageRepository(
        @SafeApi apiService: MessageAPIService,
        @UnsafeApi unsafeAPIService: MessageAPIService,
        messageDataSource: MessageDataSource,
        settingsRepository: SettingsRepository,
    ): MessageRepository = MessageRepositoryImpl(apiService, unsafeAPIService, settingsRepository, messageDataSource)

    @Provides
    @Singleton
    fun provideMessageDataSource(
        @SafeApi apiService: MessageAPIService,
        @UnsafeApi unsafeAPIService: MessageAPIService,
    ): MessageDataSource = MessageDataSourceImpl(apiService, unsafeAPIService)
}
