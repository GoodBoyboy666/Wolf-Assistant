package top.goodboyboy.wolfassistant.ui.appsetting.model

import top.goodboyboy.wolfassistant.common.Failure

sealed class VersionDomainData {
    data class Success(
        val data: VersionInfo,
    ) : VersionDomainData()

    object NOUpdate : VersionDomainData()

    data class Error(
        val error: Failure,
    ) : VersionDomainData()
}
