package top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.ServiceItem

interface ServiceRemoteDataSource {
    /**
     * 获取服务列表
     *
     * @param accessToken 令牌
     * @return DataResult
     */
    suspend fun getServiceList(accessToken: String): DataResult

    sealed class DataResult {
        data class Success(
            val data: List<ServiceItem>,
        ) : DataResult()

        data class Error(
            val error: Failure,
        ) : DataResult()
    }
}
