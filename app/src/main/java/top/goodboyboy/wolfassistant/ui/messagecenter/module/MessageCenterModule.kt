package top.goodboyboy.wolfassistant.ui.messagecenter.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.hutapi.message.MessageAPIService
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
        apiService: MessageAPIService,
        messageDataSource: MessageDataSource,
    ): MessageRepository = MessageRepositoryImpl(apiService, messageDataSource)

    @Provides
    @Singleton
    fun provideMessageDataSource(apiService: MessageAPIService): MessageDataSource = MessageDataSourceImpl(apiService)
}
