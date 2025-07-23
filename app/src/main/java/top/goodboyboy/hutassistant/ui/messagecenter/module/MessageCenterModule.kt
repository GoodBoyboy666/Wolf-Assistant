package top.goodboyboy.hutassistant.ui.messagecenter.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.hutassistant.api.hutapi.message.MessageAPIService
import top.goodboyboy.hutassistant.ui.messagecenter.datasource.MessageDataSource
import top.goodboyboy.hutassistant.ui.messagecenter.datasource.MessageDataSourceImpl
import top.goodboyboy.hutassistant.ui.messagecenter.repository.MessageRepository
import top.goodboyboy.hutassistant.ui.messagecenter.repository.MessageRepositoryImpl
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
