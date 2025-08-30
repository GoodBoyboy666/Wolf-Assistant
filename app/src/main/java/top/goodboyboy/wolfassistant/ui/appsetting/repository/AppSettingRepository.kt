package top.goodboyboy.wolfassistant.ui.appsetting.repository

import top.goodboyboy.wolfassistant.ui.appsetting.model.VersionDomainData

interface AppSettingRepository {
    suspend fun getUpdateInfo(oldVersionName: String): VersionDomainData
}
