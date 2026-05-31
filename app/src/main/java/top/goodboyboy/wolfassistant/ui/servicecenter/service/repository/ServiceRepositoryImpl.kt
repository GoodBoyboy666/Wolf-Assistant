package top.goodboyboy.wolfassistant.ui.servicecenter.service.repository

import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceCacheDataSource
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceRemoteDataSource
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository.ServiceListData
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository.ServiceListData.Failed
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository.ServiceListData.Success
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepositoryImpl
    @Inject
    constructor(
        private val serviceCacheDataSource: ServiceCacheDataSource,
        private val serviceRemoteDataSource: ServiceRemoteDataSource,
        private val logger: AppLogger,
    ) : ServiceRepository {
        override suspend fun getServiceList(accessToken: String): ServiceListData {
            logger.i("获取服务列表")
            val cache = serviceCacheDataSource.getServiceList()
            when (cache) {
                is ServiceCacheDataSource.DataResult.Error -> {
                    logger.e(cache.error.cause, "获取服务列表缓存失败")
                    return Failed(cache.error)
                }

                is ServiceCacheDataSource.DataResult.Success -> {
                    logger.i("获取服务列表: 缓存命中")
                    return Success(cache.data)
                }

                ServiceCacheDataSource.DataResult.NoCache -> {
                    logger.i("获取服务列表: 缓存未命中，请求远程数据")
                }
            }

            val remote =
                serviceRemoteDataSource.getServiceList(
                    accessToken,
                )
            when (remote) {
                is ServiceRemoteDataSource.DataResult.Error -> {
                    logger.e(remote.error.cause, "获取服务列表远程数据失败")
                    return Failed(remote.error)
                }

                is ServiceRemoteDataSource.DataResult.Success -> {
                    logger.i("获取服务列表: 远程数据获取成功")
                    serviceCacheDataSource.saveServiceList(remote.data.toList())
                    return Success(remote.data)
                }
            }
        }

        override suspend fun cleanServiceList() {
            logger.i("清除服务列表缓存")
            serviceCacheDataSource.cleanServiceList()
        }
    }
