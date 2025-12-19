package top.goodboyboy.wolfassistant.ui.appsetting.repository

import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData

interface UpdateRepository {
    suspend fun checkUpdate(
        currentVersion: String,
        enablePreRelease: Boolean,
    ): VersionDomainData
}
