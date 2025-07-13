package top.goodboyboy.hutassistant.ui.home.portal.datasource

import top.goodboyboy.hutassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.hutassistant.ui.home.portal.model.PortalInfoItem
import top.goodboyboy.hutassistant.ui.home.portal.model.RemoteDataResult

interface PortalRemoteDataSource {
    /**
     * 获取门户分类
     *
     * @return RemoteDataResult
     */
    suspend fun getPortalCategory(): RemoteDataResult<List<PortalCategoryItem>>

    /**
     * 获取门户信息
     *
     * @param portalID 门户ID
     * @return RemoteDataResult
     */
    suspend fun getPortalInfoList(portalID: String): RemoteDataResult<List<PortalInfoItem>>
}
