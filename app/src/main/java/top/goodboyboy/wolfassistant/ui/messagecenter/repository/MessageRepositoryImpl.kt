package top.goodboyboy.wolfassistant.ui.messagecenter.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import top.goodboyboy.wolfassistant.api.hutapi.SafeApi
import top.goodboyboy.wolfassistant.api.hutapi.UnsafeApi
import top.goodboyboy.wolfassistant.api.hutapi.message.MessageAPIService
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.messagecenter.datasource.MessageDataSource
import top.goodboyboy.wolfassistant.ui.messagecenter.datasource.MessageFailingPagingSource
import top.goodboyboy.wolfassistant.ui.messagecenter.datasource.MessagePagingSource
import top.goodboyboy.wolfassistant.ui.messagecenter.model.MessageItem
import top.goodboyboy.wolfassistant.ui.messagecenter.repository.MessageRepository.AppIDData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl
    @Inject
    constructor(
        @param:SafeApi val apiService: MessageAPIService,
        @param:UnsafeApi val unsafeAPIService: MessageAPIService,
        private val settingsRepository: SettingsRepository,
        val messageDataSource: MessageDataSource,
    ) : MessageRepository {
        val disableSSLCertVerification = settingsRepository.disableSSLCertVerification

        override suspend fun getMessages(
            accessToken: String,
            appID: String,
        ): Flow<PagingData<MessageItem>> {
            val disable = disableSSLCertVerification.first()
            return Pager(
                config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                pagingSourceFactory = {
                    MessagePagingSource(
                        accessToken = accessToken,
                        appID = appID,
                        apiService =
                            if (disable) {
                                unsafeAPIService
                            } else {
                                apiService
                            },
                    )
                },
            ).flow
        }

        override suspend fun getAppID(accessToken: String): AppIDData {
            val remote = messageDataSource.getAppID(accessToken)
            when (remote) {
                is MessageDataSource.DataResult.Error -> {
                    return AppIDData.Failed(remote.error)
                }

                is MessageDataSource.DataResult.Success -> {
                    return AppIDData.Success(remote.data)
                }
            }
        }

        override fun createErrorFlow(error: Throwable): Flow<PagingData<MessageItem>> =
            Pager(
                config = PagingConfig(pageSize = 1),
                pagingSourceFactory = { MessageFailingPagingSource(error) as PagingSource<Int, MessageItem> },
            ).flow
    }
