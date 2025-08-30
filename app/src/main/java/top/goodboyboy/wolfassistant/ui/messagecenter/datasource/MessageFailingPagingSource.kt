package top.goodboyboy.wolfassistant.ui.messagecenter.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * 用来返回错误信息的PagingSource
 *
 * @property error 抛出的错误
 * @constructor Create empty Message failing paging source
 */
class MessageFailingPagingSource(
    private val error: Throwable,
) : PagingSource<Int, Nothing>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Nothing> = LoadResult.Error(error)

    override fun getRefreshKey(state: PagingState<Int, Nothing>): Int? = null
}
