package top.goodboyboy.wolfassistant.ui.appsetting.datasource

import top.goodboyboy.wolfassistant.common.Failure
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionInfo

interface GitHubDataSource {
    suspend fun checkUpdateInfo(): VersionDataResult

    sealed class VersionDataResult {
        data class Success(
            val data: VersionInfo,
        ) : VersionDataResult()

        data class Error(
            val error: Failure,
        ) : VersionDataResult()
    }
}
