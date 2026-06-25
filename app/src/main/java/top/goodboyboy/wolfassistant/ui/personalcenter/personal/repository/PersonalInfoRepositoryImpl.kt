package top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository

import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoRemoteDataSource
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository.PersonalInfoData
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository.PersonalInfoData.Failed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalInfoRepositoryImpl
    @Inject
    constructor(
        private val personalInfoCacheDataSource: PersonalInfoCacheDataSource,
        private val personalInfoRemoteDataSource: PersonalInfoRemoteDataSource,
        private val logger: AppLogger,
    ) : PersonalInfoRepository {
        override suspend fun getPersonalInfo(accessToken: String): PersonalInfoData {
            logger.i("获取个人信息")
            when (val cache = personalInfoCacheDataSource.getPersonalInfo()) {
                is PersonalInfoCacheDataSource.DataResult.Error -> {}
                PersonalInfoCacheDataSource.DataResult.NoCache -> {}
                is PersonalInfoCacheDataSource.DataResult.Success -> {
                    logger.i("获取个人信息: 缓存命中")
                    return PersonalInfoData.Success(cache.info)
                }
            }
            when (val remote = personalInfoRemoteDataSource.getPersonalInfo(accessToken)) {
                is PersonalInfoRemoteDataSource.DataResult.Error -> {
                    logger.e(remote.error.cause, "获取个人信息失败")
                    return Failed(remote.error)
                }

                is PersonalInfoRemoteDataSource.DataResult.Success -> {
                    logger.i("获取个人信息: 远程数据获取成功")
                    personalInfoCacheDataSource.savePersonalInfo(remote.data)
                    return PersonalInfoData.Success(remote.data)
                }
            }
        }

        override suspend fun cleanPersonalInfoCache() {
            logger.i("清除个人信息缓存")
            personalInfoCacheDataSource.cleanPersonalInfo()
        }
    }
