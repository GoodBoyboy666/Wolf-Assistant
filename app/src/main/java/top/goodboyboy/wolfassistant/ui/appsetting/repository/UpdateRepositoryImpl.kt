package top.goodboyboy.wolfassistant.ui.appsetting.repository

import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.ui.appsetting.datasource.GitHubDataSource
import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import top.goodboyboy.wolfassistant.util.version.VersionUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepositoryImpl
    @Inject
    constructor(
        private val gitHubDataSource: GitHubDataSource,
        private val logger: AppLogger,
    ) : UpdateRepository {
        override suspend fun checkUpdate(
            currentVersion: String,
            enablePreRelease: Boolean,
        ): VersionDomainData {
            logger.i("检查更新")
            if (!enablePreRelease) {
                when (val latestVersionResult = gitHubDataSource.checkUpdateInfo()) {
                    is GitHubDataSource.VersionDataResult.Error -> {
                        logger.e(latestVersionResult.error.cause, "检查更新失败")
                        return VersionDomainData.Error(latestVersionResult.error)
                    }

                    is GitHubDataSource.VersionDataResult.Success -> {
                        val latestVerString = latestVersionResult.data.version

                        val oldVersion = VersionUtil.parse(currentVersion)
                        val newVersion = VersionUtil.parse(latestVerString)

                        return if (newVersion > oldVersion) {
                            logger.i("发现新版本")
                            VersionDomainData.Success(latestVersionResult.data)
                        } else {
                            logger.i("已是最新版本")
                            VersionDomainData.NOUpdate
                        }
                    }
                }
            } else {
                when (val latestVersionResult = gitHubDataSource.checkUpdateInfoIncludePreRelease()) {
                    is GitHubDataSource.VersionDataResult.Error -> {
                        logger.e(latestVersionResult.error.cause, "检查更新失败")
                        return VersionDomainData.Error(latestVersionResult.error)
                    }

                    is GitHubDataSource.VersionDataResult.Success -> {
                        val latestVerString = latestVersionResult.data.version

                        val oldVersion = VersionUtil.parse(currentVersion)
                        val newVersion = VersionUtil.parse(latestVerString)

                        return if (newVersion > oldVersion) {
                            logger.i("发现新版本")
                            VersionDomainData.Success(latestVersionResult.data)
                        } else {
                            logger.i("已是最新版本")
                            VersionDomainData.NOUpdate
                        }
                    }
                }
            }
        }
    }
