package top.goodboyboy.wolfassistant.ui.appsetting.repository

import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData
import javax.inject.Inject

class AppSettingRepositoryImpl
    @Inject
    constructor(
        private val updateRepository: UpdateRepository,
    ) : AppSettingRepository {
        override suspend fun getUpdateInfo(
            oldVersionName: String,
            enablePreRelease: Boolean,
        ): VersionDomainData = updateRepository.checkUpdate(oldVersionName, enablePreRelease)
    }
