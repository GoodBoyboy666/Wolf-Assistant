package top.goodboyboy.wolfassistant.ui.servicecenter.service.repository

import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceCacheDataSource
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceRemoteDataSource
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository.ServiceListData
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository.ServiceListData.Failed
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository.ServiceListData.Success
import javax.inject.Inject

class ServiceRepositoryImpl
    @Inject
    constructor(
        private val serviceCacheDataSource: ServiceCacheDataSource,
        private val serviceRemoteDataSource: ServiceRemoteDataSource,
    ) : ServiceRepository {
        override suspend fun getServiceList(
            accessToken: String,
            disableSSLCertVerification: Boolean,
        ): ServiceListData {
            val cache = serviceCacheDataSource.getServiceList()
            when (cache) {
                is ServiceCacheDataSource.DataResult.Error -> {
                    return Failed(cache.error)
                }

                is ServiceCacheDataSource.DataResult.Success -> {
                    return Success(cache.data)
                }

                ServiceCacheDataSource.DataResult.NoCache -> {}
            }

            val remote =
                serviceRemoteDataSource.getServiceList(
                    accessToken,
                    disableSSLCertVerification,
                )
            when (remote) {
                is ServiceRemoteDataSource.DataResult.Error -> {
                    return Failed(remote.error)
                }

                is ServiceRemoteDataSource.DataResult.Success -> {
                    serviceCacheDataSource.saveServiceList(remote.data.toList())
                    return Success(remote.data)
                }
            }
        }

        override suspend fun cleanServiceList() {
            serviceCacheDataSource.cleanServiceList()
        }
    }
