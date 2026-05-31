package top.goodboyboy.wolfassistant.ui.messagecenter.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.ui.messagecenter.datasource.MessageDataSource
import top.goodboyboy.wolfassistant.ui.messagecenter.datasource.MessageDataSourceImpl
import top.goodboyboy.wolfassistant.ui.messagecenter.repository.MessageRepository
import top.goodboyboy.wolfassistant.ui.messagecenter.repository.MessageRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class MessageCenterModule {
    @Binds
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    abstract fun bindMessageDataSource(impl: MessageDataSourceImpl): MessageDataSource
}
