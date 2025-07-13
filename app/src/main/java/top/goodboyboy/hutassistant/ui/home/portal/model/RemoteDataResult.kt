package top.goodboyboy.hutassistant.ui.home.portal.model

import top.goodboyboy.hutassistant.common.Failure

sealed class RemoteDataResult<out T> {
    data class Success<out T>(
        val data: T,
    ) : RemoteDataResult<T>()

    data class Error(
        val error: Failure,
    ) : RemoteDataResult<Nothing>()
}
