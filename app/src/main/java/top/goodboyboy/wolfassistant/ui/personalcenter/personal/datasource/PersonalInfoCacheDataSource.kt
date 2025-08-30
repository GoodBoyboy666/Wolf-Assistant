package top.goodboyboy.wolfassistant.ui.personalcenter.personal.datasource

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.model.PersonalInfo

interface PersonalInfoCacheDataSource {
    /**
     * 获取用户信息
     *
     * @return DataResult
     */
    suspend fun getPersonalInfo(): DataResult

    /**
     * 缓存用户信息
     *
     * @param info 信息实例
     * @return SaveResult
     */
    suspend fun savePersonalInfo(info: PersonalInfo): SaveResult

    /**
     * 清除用户信息缓存
     *
     * @return CleanResult
     */
    suspend fun cleanPersonalInfo(): CleanResult

    sealed class DataResult {
        data class Success(
            val info: PersonalInfo,
        ) : DataResult()

        object NoCache : DataResult()

        data class Error(
            val error: Failure,
        ) : DataResult()
    }

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
