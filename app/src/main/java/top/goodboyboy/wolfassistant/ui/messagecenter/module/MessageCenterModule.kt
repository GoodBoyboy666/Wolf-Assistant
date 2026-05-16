package top.goodboyboy.wolfassistant.ui.messagecenter.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.api.hutapi.message.MessageAPIService
import top.goodboyboy.wolfassistant.log.AppLogger
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
        logger: AppLogger,
    ): MessageRepository = MessageRepositoryImpl(apiService, messageDataSource, logger)

    @Provides
    @Singleton
    fun provideMessageDataSource(
        apiService: MessageAPIService,
        logger: AppLogger,
    ): MessageDataSource = MessageDataSourceImpl(apiService, logger)
}
