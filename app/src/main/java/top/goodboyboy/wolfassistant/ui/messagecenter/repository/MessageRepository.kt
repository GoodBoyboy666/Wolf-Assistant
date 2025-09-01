package top.goodboyboy.wolfassistant.ui.messagecenter.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.messagecenter.model.MessageItem

interface MessageRepository {
    /**
     * 获取信息
     *
     * @param accessToken 令牌
     * @param appID appid（message）
     * @return Flow
     */
    suspend fun getMessages(
        accessToken: String,
        appID: String,
        disableSSLCertVerification: Boolean,
    ): Flow<PagingData<MessageItem>>

    /**
     * 获取appid
     *
     * @param accessToken 令牌
     * @return
     */
    suspend fun getAppID(
        accessToken: String,
        disableSSLCertVerification: Boolean,
    ): AppIDData

    fun createErrorFlow(error: Throwable): Flow<PagingData<MessageItem>>

    sealed class AppIDData {
        data class Success(
            val data: List<String>,
        ) : AppIDData()

        data class Failed(
            val reason: Failure,
        ) : AppIDData()
    }
}
