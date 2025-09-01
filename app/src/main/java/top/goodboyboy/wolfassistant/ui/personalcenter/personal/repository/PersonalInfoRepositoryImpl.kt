package top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository

import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoCacheDataSource
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource.PersonalInfoRemoteDataSource
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository.PersonalInfoData
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository.PersonalInfoData.Failed

class PersonalInfoRepositoryImpl(
    private val personalInfoCacheDataSource: PersonalInfoCacheDataSource,
    private val personalInfoRemoteDataSource: PersonalInfoRemoteDataSource,
) : PersonalInfoRepository {
    override suspend fun getPersonalInfo(
        accessToken: String,
        disableSSLCertVerification: Boolean,
    ): PersonalInfoData {
        val cache = personalInfoCacheDataSource.getPersonalInfo()
        when (cache) {
            is PersonalInfoCacheDataSource.DataResult.Error -> {}
            PersonalInfoCacheDataSource.DataResult.NoCache -> {}
            is PersonalInfoCacheDataSource.DataResult.Success -> {
                return PersonalInfoData.Success(cache.info)
            }
        }
        val remote = personalInfoRemoteDataSource.getPersonalInfo(accessToken, disableSSLCertVerification)
        when (remote) {
            is PersonalInfoRemoteDataSource.DataResult.Error ->
                return Failed(remote.error)

            is PersonalInfoRemoteDataSource.DataResult.Success -> {
                personalInfoCacheDataSource.savePersonalInfo(remote.data)
                return PersonalInfoData.Success(remote.data)
            }
        }
    }

    override suspend fun cleanPersonalInfoCache() {
        personalInfoCacheDataSource.cleanPersonalInfo()
    }
}
