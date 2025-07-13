package top.goodboyboy.hutassistant.ui.home.portal.datasource

import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.ui.home.portal.model.CacheDataResult
import top.goodboyboy.hutassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.hutassistant.ui.home.portal.model.PortalInfoItem

interface PortalCacheDataSource {
    /**
     * 获取门户分类信息
     *
     * @param expirationInterval 缓存过期时间
     * @return CacheDataResult
     */
    suspend fun getPortalCategory(expirationInterval: Int = 12): CacheDataResult<List<PortalCategoryItem>>

    /**
     * 获取门户信息列表
     *
     * @param portalID 门户ID
     * @param expirationInterval 过期时间
     * @return CacheDataResult
     */
    suspend fun getPortalInfoList(
        portalID: String,
        expirationInterval: Int = 6,
    ): CacheDataResult<List<PortalInfoItem>>

    /**
     * 缓存门户分类信息
     *
     * @param categories 门户分类列表
     * @return SaveResult
     */
    suspend fun savePortalCategory(categories: List<PortalCategoryItem>): SaveResult

    /**
     * 缓存门户信息列表
     *
     * @param portalID 门户ID
     * @param infos 信息列表
     * @return SaveResult
     */
    suspend fun savePortalInfoList(
        portalID: String,
        infos: List<PortalInfoItem>,
    ): SaveResult

    /**
     * 清除门户分类缓存
     *
     * @return CleanResult
     */
    suspend fun cleanPortalCategoryCache(): CleanResult

    /**
     * 清理门户信息列表缓存
     *
     * @return CleanResult
     */
    suspend fun cleanPortalInfoCache(): CleanResult

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
