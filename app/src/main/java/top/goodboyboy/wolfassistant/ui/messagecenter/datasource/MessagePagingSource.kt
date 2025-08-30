package top.goodboyboy.wolfassistant.ui.messagecenter.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.JsonParser
import top.goodboyboy.wolfassistant.api.hutapi.message.MessageAPIService
import top.goodboyboy.wolfassistant.ui.messagecenter.model.MessageItem

/**
 * Message paging source
 *
 * @property accessToken 令牌
 * @property appID appid（message）
 * @property apiService MessageAPIService依赖注入
 * @constructor Create empty Message paging source
 */
class MessagePagingSource(
    private val accessToken: String,
    private val appID: String = "",
    private val apiService: MessageAPIService,
) : PagingSource<Int, MessageItem>() {
    companion object {
        const val INITIAL_PAGE_INDEX = 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MessageItem> {
        val page = params.key ?: INITIAL_PAGE_INDEX
        return try {
            val response =
                apiService.getNotice(
                    accessToken = accessToken,
                    pageIndex = page,
                    appId = appID,
                )
            val messageList = mutableListOf<MessageItem>()
            response.use {
                val test = it.string()
                JsonParser.parseString(test).asJsonObject.getAsJsonArray("data").forEach {
                    val item = it.asJsonObject
                    val message =
                        MessageItem(
                            title = item.get("title")?.asString ?: "",
                            author =
                                item.get("signOff")?.let { element ->
                                    if (element.isJsonNull) "" else element.asString
                                } ?: "",
                            editTime = item.get("editTime")?.asString ?: "",
                            content = item.get("content")?.asString ?: "",
                        )
                    messageList.add(message)
                }
            }

            val nextKey =
                if (messageList.isEmpty()) {
                    null
                } else {
                    page + 1
                }

            LoadResult.Page(
                data = messageList,
                prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MessageItem>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}
