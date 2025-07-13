package top.goodboyboy.hutassistant.ui.messagecenter.datasource

import top.goodboyboy.hutassistant.common.Failure

interface MessageDataSource {
    /**
     * 获取app id（message）
     *
     * @param accessToken
     * @return
     */
    suspend fun getAppID(accessToken: String): DataResult

    sealed class DataResult {
        data class Success(
            val data: List<String>,
        ) : DataResult()

        data class Error(
            val error: Failure,
        ) : DataResult()
    }
}
