package top.goodboyboy.hutassistant.ui.appsetting

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.goodboyboy.hutassistant.settings.SettingsRepository
import top.goodboyboy.hutassistant.ui.home.portal.repository.PortalRepository
import top.goodboyboy.hutassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import top.goodboyboy.hutassistant.ui.schedulecenter.repository.ScheduleCenterRepository
import top.goodboyboy.hutassistant.ui.servicecenter.service.repository.ServiceRepository
import top.goodboyboy.hutassistant.util.CacheUtil
import javax.inject.Inject

@HiltViewModel
class SettingViewModel
    @Inject
    constructor(
        private val portalRepository: PortalRepository,
        private val serviceRepository: ServiceRepository,
        private val scheduleCenterRepository: ScheduleCenterRepository,
        private val personalInfoRepository: PersonalInfoRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _cacheSize = mutableStateOf("正在计算中……")
        val cacheSize: State<String> get() = _cacheSize

        suspend fun getTotalCacheSize(context: Context) {
            withContext(Dispatchers.IO) {
                val size = CacheUtil.getTotalCacheSize(context)
                withContext(Dispatchers.Main) {
                    _cacheSize.value = size
                }
            }
        }

        suspend fun cleanAllCache(context: Context) {
            withContext(Dispatchers.IO) {
                CacheUtil.clearAllCache(context)
                getTotalCacheSize(context)
            }
        }

        suspend fun logout(context: Context) {
            portalRepository.cleanCache()
            serviceRepository.cleanServiceList()
            scheduleCenterRepository.cleanScheduleCache()
            personalInfoRepository.cleanPersonalInfoCache()
            settingsRepository.cleanAllData()
            cleanAllCache(context)
        }
    }
