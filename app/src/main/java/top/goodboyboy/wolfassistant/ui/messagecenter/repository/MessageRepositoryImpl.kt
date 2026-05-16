package top.goodboyboy.wolfassistant.ui.messagecenter.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import top.goodboyboy.wolfassistant.api.hutapi.message.MessageAPIService
import top.goodboyboy.wolfassistant.log.AppLogger
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
        val apiService: MessageAPIService,
        val messageDataSource: MessageDataSource,
        private val logger: AppLogger,
    ) : MessageRepository {
        override suspend fun getMessages(
            accessToken: String,
            appID: String,
        ): Flow<PagingData<MessageItem>> {
            logger.i("创建消息分页流")
            return Pager(
                config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                pagingSourceFactory = {
                    MessagePagingSource(
                        accessToken = accessToken,
                        appID = appID,
                        apiService = apiService,
                        logger = logger,
                    )
                },
            ).flow
        }

        override suspend fun getAppID(accessToken: String): AppIDData {
            logger.i("获取消息应用ID")
            val remote = messageDataSource.getAppID(accessToken)
            return when (remote) {
                is MessageDataSource.DataResult.Error -> {
                    logger.e(remote.error.cause, "获取消息应用ID失败")
                    AppIDData.Failed(remote.error)
                }

                is MessageDataSource.DataResult.Success -> {
                    logger.i("获取消息应用ID成功")
                    AppIDData.Success(remote.data)
                }
            }
        }

        override fun createErrorFlow(error: Throwable): Flow<PagingData<MessageItem>> {
            logger.e(error, "创建错误消息流")
            return Pager(
                config = PagingConfig(pageSize = 1),
                pagingSourceFactory = { MessageFailingPagingSource(error) as PagingSource<Int, MessageItem> },
            ).flow
        }
    }
