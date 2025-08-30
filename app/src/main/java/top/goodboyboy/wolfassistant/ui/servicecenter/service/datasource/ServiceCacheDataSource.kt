package top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.ServiceItem

interface ServiceCacheDataSource {
    /**
     * 获取服务列表
     *
     * @return DataResult
     */
    suspend fun getServiceList(): DataResult

    /**
     * 缓存服务列表
     *
     * @param list 服务列表
     * @return SaveResult
     */
    suspend fun saveServiceList(list: List<ServiceItem>): SaveResult

    /**
     * 清除服务列表缓存
     *
     * @return CleanResult
     */
    suspend fun cleanServiceList(): CleanResult

    sealed class DataResult {
        data class Success(
            val data: List<ServiceItem>,
        ) : DataResult()

        object NoCache : DataResult()

        data class Error(
            val error: Failure,
        ) : DataResult()
    }

    sealed class SaveResult {
        object Success : SaveResult()

        data class Error(
            val error: Failure,
        ) : SaveResult()
    }

    sealed class CleanResult {
        object Success : CleanResult()

        data class Error(
            val error: Failure,
        ) : CleanResult()
    }
}
