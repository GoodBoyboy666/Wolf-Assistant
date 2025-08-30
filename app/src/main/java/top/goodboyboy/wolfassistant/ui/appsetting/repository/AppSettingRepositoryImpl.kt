package top.goodboyboy.wolfassistant.ui.appsetting.repository

import top.goodboyboy.wolfassistant.ui.appsetting.VersionUtil
import top.goodboyboy.wolfassistant.ui.appsetting.datasource.GitHubDataSource
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import javax.inject.Inject

class AppSettingRepositoryImpl
    @Inject
    constructor(
        private val gitHubDataSource: GitHubDataSource,
    ) : AppSettingRepository {
        override suspend fun getUpdateInfo(oldVersionName: String): VersionDomainData {
            val oldVersionNameItem = VersionUtil.getVersionNameItem(oldVersionName.removePrefix("v"))
            val latestVersionResult = gitHubDataSource.checkUpdateInfo()
            when (latestVersionResult) {
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
