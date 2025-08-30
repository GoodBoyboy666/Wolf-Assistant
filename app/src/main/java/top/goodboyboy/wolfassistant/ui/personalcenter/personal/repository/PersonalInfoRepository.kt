package top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.model.PersonalInfo

interface PersonalInfoRepository {
    /**
     * 获取用户信息
     *
     * @param accessToken 令牌
     * @return DataResult
     */
    suspend fun getPersonalInfo(accessToken: String): PersonalInfoData

    /**
     * 清除用户信息缓存
     *
     */
    suspend fun cleanPersonalInfoCache()

    sealed class PersonalInfoData {
        data class Success(
            val data: PersonalInfo,
        ) : PersonalInfoData()

        data class Failed(
            val error: Failure,
        ) : PersonalInfoData()
    }
}
