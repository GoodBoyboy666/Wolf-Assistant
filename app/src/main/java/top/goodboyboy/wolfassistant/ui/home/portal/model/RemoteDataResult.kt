package top.goodboyboy.wolfassistant.ui.home.portal.model

import top.goodboyboy.wolfassistant.common.Failure

sealed class RemoteDataResult<out T> {
    data class Success<out T>(
        val data: T,
    ) : RemoteDataResult<T>()

    data class Error(
        val error: Failure,
    ) : RemoteDataResult<Nothing>()
}
