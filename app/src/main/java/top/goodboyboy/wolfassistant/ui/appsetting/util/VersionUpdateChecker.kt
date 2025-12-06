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

                    val isNewer = when {
                        latestVersionNameItem.majorVersionNumber > oldVersionNameItem.majorVersionNumber -> true
                        latestVersionNameItem.majorVersionNumber < oldVersionNameItem.majorVersionNumber -> false
                        latestVersionNameItem.secondaryVersionNumber > oldVersionNameItem.secondaryVersionNumber -> true
                        latestVersionNameItem.secondaryVersionNumber < oldVersionNameItem.secondaryVersionNumber -> false
                        latestVersionNameItem.revisionVersionNumber > oldVersionNameItem.revisionVersionNumber -> true
                        else -> false
                    }

                    return if (isNewer) {
                        VersionDomainData.Success(latestVersionResult.data)
                    } else {
                        VersionDomainData.NOUpdate
                    }
                }
            }
        }
    }
