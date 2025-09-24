package top.goodboyboy.wolfassistant.ui.home.portal.repository

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalInfoItem

interface PortalRepository {
    /**
     * 获取门户分类列表
     *
     * @return PortalData
     */
    suspend fun getPortalCategory(accessToken: String): PortalData<List<PortalCategoryItem>>

    /**
     * 获取门户信息列表
     *
     * @param portalID 门户ID
     * @return PortalData
     */
    suspend fun getPortalInfoList(portalID: String): PortalData<List<PortalInfoItem>>

    /**
     * 清理缓存
     *
     */
    suspend fun cleanCache()

    sealed class PortalData<out T> {
        data class Success<out T>(
            val data: T,
        ) : PortalData<T>()

        data class Failed(
            val e: Failure,
        ) : PortalData<Nothing>()
    }
}
