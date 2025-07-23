package top.goodboyboy.hutassistant.ui.appsetting.datasource

import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.ui.appsetting.model.VersionInfo

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
