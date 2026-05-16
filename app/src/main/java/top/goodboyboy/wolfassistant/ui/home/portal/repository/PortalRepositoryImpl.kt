package top.goodboyboy.wolfassistant.ui.home.portal.repository

import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalCacheDataSource
import top.goodboyboy.wolfassistant.ui.home.portal.datasource.PortalRemoteDataSource
import top.goodboyboy.wolfassistant.ui.home.portal.model.CacheDataResult
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalInfoItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.RemoteDataResult
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository.PortalData
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository.PortalData.Failed
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository.PortalData.Success
import javax.inject.Inject

class PortalRepositoryImpl
    @Inject
    constructor(
        private val portalRemoteDataSource: PortalRemoteDataSource,
        private val portalCacheDataSource: PortalCacheDataSource,
        private val logger: AppLogger,
    ) : PortalRepository {
        override suspend fun getPortalCategory(accessToken: String): PortalData<List<PortalCategoryItem>> {
            logger.i("获取门户分类")
            val getCacheResult = portalCacheDataSource.getPortalCategory(12)
            when (getCacheResult) {
                is CacheDataResult.Error -> {
                    logger.e(getCacheResult.error.cause, "获取门户分类失败")
                    return Failed(getCacheResult.error)
                }

                CacheDataResult.NoCache -> {}
                is CacheDataResult.Success<List<PortalCategoryItem>> -> {
                    logger.i("获取门户分类成功")
                    return Success(getCacheResult.data)
                }
            }
            val result =
                portalRemoteDataSource.getPortalCategory(
                    accessToken,
                )
            when (result) {
                is RemoteDataResult.Error -> {
                    logger.e(result.error.cause, "获取门户分类失败")
                    return Failed(result.error)
                }

                is RemoteDataResult.Success<List<PortalCategoryItem>> -> {
                    val data = result.data
                    // 懒得when了（doge
                    portalCacheDataSource.savePortalCategory(data)
                    logger.i("获取门户分类成功")
                    return Success(data)
                }
            }
        }

        override suspend fun getPortalInfoList(portalID: String): PortalData<List<PortalInfoItem>> {
            logger.i("获取门户信息列表")
            val getCacheResult = portalCacheDataSource.getPortalInfoList(portalID, 6)
            when (getCacheResult) {
                is CacheDataResult.Error -> {
                    logger.e(getCacheResult.error.cause, "获取门户信息列表失败")
                    return Failed(getCacheResult.error)
                }

                CacheDataResult.NoCache -> {}
                is CacheDataResult.Success<List<PortalInfoItem>> -> {
                    logger.i("获取门户信息列表成功")
                    return Success(getCacheResult.data)
                }
            }
            val result =
                portalRemoteDataSource.getPortalInfoList(
                    portalID,
                )
            when (result) {
                is RemoteDataResult.Error -> {
                    logger.e(result.error.cause, "获取门户信息列表失败")
                    return Failed(result.error)
                }

                is RemoteDataResult.Success<List<PortalInfoItem>> -> {
                    val data = result.data
                    // 懒得when了（doge
                    portalCacheDataSource.savePortalInfoList(portalID, data)
                    logger.i("获取门户信息列表成功")
                    return Success(data)
                }
            }
        }

        override suspend fun cleanCache() {
            logger.i("清除门户缓存")
            portalCacheDataSource.cleanPortalCategoryCache()
            portalCacheDataSource.cleanPortalInfoCache()
        }
    }
