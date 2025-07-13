package top.goodboyboy.hutassistant.ui.personalcenter.personal.datasource

import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.ui.personalcenter.personal.model.PersonalInfo

interface PersonalInfoRemoteDataSource {
    /**
     * 获取用户信息
     *
     * @return DataResult
     */
    suspend fun getPersonalInfo(accessToken: String): DataResult

    sealed class DataResult {
        data class Success(
            val data: PersonalInfo,
        ) : DataResult()

        data class Error(
            val error: Failure,
        ) : DataResult()
    }
}
