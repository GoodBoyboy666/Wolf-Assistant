package top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource

import okio.IOException
import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.room.dao.ServiceItemDao
import top.goodboyboy.wolfassistant.room.dao.TokenKeyNameDao
import top.goodboyboy.wolfassistant.room.entity.ServiceItemEntity
import top.goodboyboy.wolfassistant.room.entity.TokenKeyNameEntity
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceCacheDataSource.CleanResult
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceCacheDataSource.DataResult
import top.goodboyboy.wolfassistant.ui.servicecenter.service.datasource.ServiceCacheDataSource.SaveResult
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.ServiceItem
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.TokenKeyName
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceCacheDataSourceImpl
    @Inject
    constructor(
        private val serviceItemDao: ServiceItemDao,
        private val tokenKeyNameDao: TokenKeyNameDao,
        private val logger: AppLogger,
    ) : ServiceCacheDataSource {
        override suspend fun getServiceList(): DataResult {
            val list = mutableListOf<ServiceItem>()
            try {
                logger.tag("ServiceCache").i("读取服务列表缓存")
                val serviceItemEntity = serviceItemDao.getAllServiceItemEntity()
                if (serviceItemEntity.isEmpty()) {
                    return DataResult.NoCache
                }
                serviceItemEntity
                    .forEach { item ->
                        if (item.tokenAccept != null) {
                            val tokenKeyNameEntity = tokenKeyNameDao.getData(item.tokenAccept)
                            if (tokenKeyNameEntity != null) {
                                val tokenKeyName =
                                    TokenKeyName(
                                        headerTokenKeyName = tokenKeyNameEntity.headerTokenKeyName ?: "",
                                        urlTokenKeyName = tokenKeyNameEntity.urlTokenKeyName ?: "",
                                    )
                                val serviceItem =
                                    ServiceItem(
                                        imageUrl = item.imageUrl,
                                        text = item.text,
                                        serviceUrl = item.serviceUrl,
                                        tokenKeyName,
                                    )
                                list.add(serviceItem)
                            } else {
                                val serviceItem =
                                    ServiceItem(
                                        imageUrl = item.imageUrl,
                                        text = item.text,
                                        serviceUrl = item.serviceUrl,
                                        null,
                                    )
                                list.add(serviceItem)
                            }
                        } else {
                            val serviceItem =
                                ServiceItem(
                                    imageUrl = item.imageUrl,
                                    text = item.text,
                                    serviceUrl = item.serviceUrl,
                                    null,
                                )
                            list.add(serviceItem)
                        }
                    }
                return DataResult.Success(list.toList())
            } catch (e: IOException) {
                logger.e(e, "读取服务列表缓存时发生IO异常")
                return DataResult.Error(Failure.IOError("获取服务列表缓存时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                logger.e(e, "读取服务列表缓存时发生未知异常")
                return DataResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun saveServiceList(list: List<ServiceItem>): SaveResult {
            try {
                logger.tag("ServiceCache").i("写入服务列表缓存")
                for (item in list) {
                    val tokenKeyName =
                        TokenKeyNameEntity(
                            headerTokenKeyName = item.tokenAccept?.headerTokenKeyName,
                            urlTokenKeyName = item.tokenAccept?.urlTokenKeyName,
                        )
                    val tokenKeyNameID = tokenKeyNameDao.insert(tokenKeyName)
                    val serviceItemEntity =
                        ServiceItemEntity(
                            imageUrl = item.imageUrl,
                            text = item.text,
                            serviceUrl = item.serviceUrl,
                            tokenAccept = tokenKeyNameID.toInt(),
                        )
                    serviceItemDao.insert(serviceItemEntity)
                }
                return SaveResult.Success
            } catch (e: IOException) {
                logger.e(e, "写入服务列表缓存时发生IO异常")
                return SaveResult.Error(Failure.IOError("保存服务列表缓存时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                logger.e(e, "写入服务列表缓存时发生未知异常")
                return SaveResult.Error(Failure.UnknownError(e))
            }
        }

        override suspend fun cleanServiceList(): CleanResult {
            try {
                logger.tag("ServiceCache").i("清除服务列表缓存")
                serviceItemDao.cleanServiceList()
                tokenKeyNameDao.cleanTokenKeyName()
                return CleanResult.Success
            } catch (e: IOException) {
                logger.e(e, "清除服务列表缓存时发生IO异常")
                return CleanResult.Error(Failure.IOError("清除服务列表缓存时出现IO异常" + e.message, e))
            } catch (e: Exception) {
                logger.e(e, "清除服务列表缓存时发生未知异常")
                return CleanResult.Error(Failure.UnknownError(e))
            }
        }
    }
