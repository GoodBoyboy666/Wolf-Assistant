package top.goodboyboy.hutassistant.ui.home.portal.model

import top.goodboyboy.hutassistant.common.Failure

sealed class CacheDataResult<out T> {
    data class Success<out T>(
        val data: T,
    ) : CacheDataResult<T>()

    object NoCache : CacheDataResult<Nothing>()

    data class Error(
        val error: Failure,
    ) : CacheDataResult<Nothing>()
}
