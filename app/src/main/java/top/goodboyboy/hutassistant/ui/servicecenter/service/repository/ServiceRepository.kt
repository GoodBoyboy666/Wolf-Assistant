package top.goodboyboy.hutassistant.ui.servicecenter.service.repository

import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.ui.servicecenter.service.model.ServiceItem

interface ServiceRepository {
    /**
     * 获取服务列表
     *
     * @param accessToken 令牌
     * @return DataResult
     */
    suspend fun getServiceList(accessToken: String): ServiceListData

    /**
     * 清除服务列表缓存
     *
     */
    suspend fun cleanServiceList()

    sealed class ServiceListData {
        data class Success(
            val data: List<ServiceItem>,
        ) : ServiceListData()

        data class Failed(
            val error: Failure,
        ) : ServiceListData()
    }
}
