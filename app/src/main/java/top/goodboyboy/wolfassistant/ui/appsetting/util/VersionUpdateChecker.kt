package top.goodboyboy.wolfassistant.ui.appsetting.util

import top.goodboyboy.wolfassistant.ui.appsetting.datasource.GitHubDataSource
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import javax.inject.Inject

class VersionUpdateChecker
    @Inject
    constructor(
        private val gitHubDataSource: GitHubDataSource,
    ) {
        suspend fun checkUpdate(oldVersionName: String): VersionDomainData {
            val oldVersionNameItem = VersionUtil.getVersionNameItem(oldVersionName.removePrefix("v"))
            when (val latestVersionResult = gitHubDataSource.checkUpdateInfo()) {
                is GitHubDataSource.VersionDataResult.Error -> {
                    return VersionDomainData.Error(latestVersionResult.error)
                }

                is GitHubDataSource.VersionDataResult.Success -> {
                    val latestVersionNameItem = latestVersionResult.data.versionNameItem
                    if (latestVersionNameItem.majorVersionNumber > oldVersionNameItem.majorVersionNumber ||
                        latestVersionNameItem.secondaryVersionNumber > oldVersionNameItem.secondaryVersionNumber ||
                        latestVersionNameItem.revisionVersionNumber > oldVersionNameItem.revisionVersionNumber
                    ) {
                        return VersionDomainData.Success(latestVersionResult.data)
                    } else {
                        return VersionDomainData.NOUpdate
                    }
                }
            }
        }
    }
