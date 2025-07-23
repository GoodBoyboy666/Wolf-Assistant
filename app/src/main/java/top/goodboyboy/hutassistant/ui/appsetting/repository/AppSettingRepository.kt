package top.goodboyboy.hutassistant.ui.appsetting.repository

import top.goodboyboy.hutassistant.ui.appsetting.model.VersionDomainData

interface AppSettingRepository {
    suspend fun getUpdateInfo(oldVersionName: String): VersionDomainData
}
