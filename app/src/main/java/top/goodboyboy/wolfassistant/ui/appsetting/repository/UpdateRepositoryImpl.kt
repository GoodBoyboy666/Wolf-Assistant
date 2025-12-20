package top.goodboyboy.wolfassistant.ui.appsetting.repository

import top.goodboyboy.wolfassistant.ui.appsetting.datasource.GitHubDataSource
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import top.goodboyboy.wolfassistant.util.version.VersionUtil
import javax.inject.Inject

class UpdateRepositoryImpl
    @Inject
    constructor(
        private val gitHubDataSource: GitHubDataSource,
    ) : UpdateRepository {
        override suspend fun checkUpdate(
            currentVersion: String,
            enablePreRelease: Boolean,
        ): VersionDomainData {
            if (!enablePreRelease) {
                when (val latestVersionResult = gitHubDataSource.checkUpdateInfo()) {
                    is GitHubDataSource.VersionDataResult.Error -> {
                        return VersionDomainData.Error(latestVersionResult.error)
                    }

                    is GitHubDataSource.VersionDataResult.Success -> {
                        val latestVerString = latestVersionResult.data.version

                        val oldVersion = VersionUtil.parse(currentVersion)
                        val newVersion = VersionUtil.parse(latestVerString)

                        return if (newVersion > oldVersion) {
                            VersionDomainData.Success(latestVersionResult.data)
                        } else {
                            VersionDomainData.NOUpdate
                        }
                    }
                }
            } else {
                when (val latestVersionResult = gitHubDataSource.checkUpdateInfoIncludePreRelease()) {
                    is GitHubDataSource.VersionDataResult.Error -> {
                        return VersionDomainData.Error(latestVersionResult.error)
                    }

                    is GitHubDataSource.VersionDataResult.Success -> {
                        val latestVerString = latestVersionResult.data.version

                        val oldVersion = VersionUtil.parse(currentVersion)
                        val newVersion = VersionUtil.parse(latestVerString)

                        return if (newVersion > oldVersion) {
                            VersionDomainData.Success(latestVersionResult.data)
                        } else {
                            VersionDomainData.NOUpdate
                        }
                    }
                }
            }
        }
    }
