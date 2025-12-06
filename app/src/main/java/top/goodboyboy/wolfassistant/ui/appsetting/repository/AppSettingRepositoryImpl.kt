package top.goodboyboy.wolfassistant.ui.appsetting.repository

import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import top.goodboyboy.wolfassistant.ui.appsetting.util.VersionUpdateChecker
import javax.inject.Inject

class AppSettingRepositoryImpl
    @Inject
    constructor(
        private val versionUpdateChecker: VersionUpdateChecker,
    ) : AppSettingRepository {
        override suspend fun getUpdateInfo(oldVersionName: String): VersionDomainData =
            versionUpdateChecker.checkUpdate(oldVersionName)
    }
