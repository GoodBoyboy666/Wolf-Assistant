package top.goodboyboy.wolfassistant.ui.home.portal.repository

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
    ) : PortalRepository {
        override suspend fun getPortalCategory(): PortalData<List<PortalCategoryItem>> {
            val getCacheResult = portalCacheDataSource.getPortalCategory(12)
            when (getCacheResult) {
                is CacheDataResult.Error -> {
                    return Failed(getCacheResult.error)
                }

                CacheDataResult.NoCache -> {}
                is CacheDataResult.Success<List<PortalCategoryItem>> -> {
                    return Success(getCacheResult.data)
                }
            }
            val result = portalRemoteDataSource.getPortalCategory()
            when (result) {
                is RemoteDataResult.Error -> {
                    return Failed(result.error)
                }

                is RemoteDataResult.Success<List<PortalCategoryItem>> -> {
                    val data = result.data
                    // 懒得when了（doge
                    portalCacheDataSource.savePortalCategory(data)
                    return Success(data)
                }
            }
        }

        override suspend fun getPortalInfoList(portalID: String): PortalData<List<PortalInfoItem>> {
            val getCacheResult = portalCacheDataSource.getPortalInfoList(portalID, 6)
            when (getCacheResult) {
                is CacheDataResult.Error -> {
                    return Failed(getCacheResult.error)
                }

                CacheDataResult.NoCache -> {}
                is CacheDataResult.Success<List<PortalInfoItem>> -> {
                    return Success(getCacheResult.data)
                }
            }
            val result = portalRemoteDataSource.getPortalInfoList(portalID)
            when (result) {
                is RemoteDataResult.Error -> {
                    return Failed(result.error)
                }

                is RemoteDataResult.Success<List<PortalInfoItem>> -> {
                    val data = result.data
                    // 懒得when了（doge
                    portalCacheDataSource.savePortalInfoList(portalID, data)
                    return Success(data)
                }
            }
        }

        override suspend fun cleanCache() {
            portalCacheDataSource.cleanPortalCategoryCache()
            portalCacheDataSource.cleanPortalInfoCache()
        }
    }
