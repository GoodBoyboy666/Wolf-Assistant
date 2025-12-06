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
            val oldItem = VersionUtil.getVersionNameItem(oldVersionName.removePrefix("v"))
            when (val latestVersionResult = gitHubDataSource.checkUpdateInfo()) {
                is GitHubDataSource.VersionDataResult.Error -> {
                    return VersionDomainData.Error(latestVersionResult.error)
                }

                is GitHubDataSource.VersionDataResult.Success -> {
                    val latestItem = latestVersionResult.data.versionNameItem

                    val isNewer =
                        when {
                            latestItem.majorVersionNumber > oldItem.majorVersionNumber -> true
                            latestItem.majorVersionNumber < oldItem.majorVersionNumber -> false
                            latestItem.secondaryVersionNumber > oldItem.secondaryVersionNumber -> true
                            latestItem.secondaryVersionNumber < oldItem.secondaryVersionNumber -> false
                            latestItem.revisionVersionNumber > oldItem.revisionVersionNumber -> true
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
