package top.goodboyboy.hutassistant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.goodboyboy.hutassistant.settings.SettingsRepository
import top.goodboyboy.hutassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.hutassistant.ui.home.portal.model.PortalInfoItem
import top.goodboyboy.hutassistant.ui.home.portal.repository.PortalRepository
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val portalRepository: PortalRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        sealed class PortalState {
            object Idle : PortalState()

            object Loading : PortalState()

            object Success : PortalState()

            data class Failed(
                val message: String,
            ) : PortalState()
        }

        private val _timeTalk = MutableStateFlow("今天过得怎么样？")
        val timeTalk: StateFlow<String> = _timeTalk.asStateFlow()

        val userName = settingsRepository.userNameFlow

        private val _portalCategoryList = MutableStateFlow<List<PortalCategoryItem>>(emptyList())
        val portalCategoryList: StateFlow<List<PortalCategoryItem>> = _portalCategoryList.asStateFlow()

        private val _portalInfoList = MutableStateFlow<List<List<PortalInfoItem>>>(emptyList())
        val portalInfoList: StateFlow<List<List<PortalInfoItem>>> = _portalInfoList.asStateFlow()

        private val _portalState = MutableStateFlow<PortalState>(PortalState.Idle)
        val portalState: StateFlow<PortalState> = _portalState.asStateFlow()

        fun loadTimeTalk() {
            val currentTime = LocalTime.now()
            val time1 = LocalTime.of(7, 0)
            val time2 = LocalTime.of(11, 0)
            val time3 = LocalTime.of(14, 0)
            val time4 = LocalTime.of(17, 0)
            val time5 = LocalTime.of(19, 0)
            val time6 = LocalTime.of(22, 0)
            val time7 = LocalTime.of(23, 59)
            val time8 = LocalTime.of(2, 0)
            val time9 = LocalTime.of(5, 0)

            val range1 = currentTime.isAfter(time1) && currentTime.isBefore(time2)
            val range2 = currentTime.isAfter(time2) && currentTime.isBefore(time3)
            val range3 = currentTime.isAfter(time3) && currentTime.isBefore(time4)
            val range4 = currentTime.isAfter(time4) && currentTime.isBefore(time5)
            val range5 = currentTime.isAfter(time5) && currentTime.isBefore(time6)
            val range6 = currentTime.isAfter(time6) && currentTime.isBefore(time7)
            val range7 = currentTime.isAfter(time7) && currentTime.isBefore(time8)
            val range8 = currentTime.isAfter(time8) && currentTime.isBefore(time9)
            val range9 = currentTime.isAfter(time9) && currentTime.isBefore(time1)

            if (range1) {
                _timeTalk.value = "新的一天又开始了，祝你过得快乐!"
            } else if (range2) {
                _timeTalk.value = "该吃午饭啦！有什么好吃的？您有中午休息的好习惯吗？"
            } else if (range3) {
                _timeTalk.value = "下午好！外面的天气好吗？记得朵朵白云曾捎来朋友殷殷的祝福。"
            } else if (range4) {
                _timeTalk.value = "太阳落山了！快看看夕阳吧！如果外面下雨，就不必了 ^_^"
            } else if (range5) {
                _timeTalk.value = "晚上好，小伙伴今天的心情怎么样？"
            } else if (range6) {
                _timeTalk.value = "这么晚了，小伙伴还在上网？早点洗洗睡吧，睡前记得洗洗脸喔！"
            } else if (range7) {
                _timeTalk.value = "现在已经过凌晨了，身体是无价的资本喔，早点休息吧！"
            } else if (range8) {
                _timeTalk.value = "该休息了，身体可是革命的本钱啊！"
            } else if (range9) {
                _timeTalk.value = "快要熬穿啦，赶紧去补补觉吧！"
            }
        }

        /**
         * 加载门户分类列表
         *
         */
        suspend fun loadPortalCategories() {
            val categories = portalRepository.getPortalCategory()
            when (categories) {
                is PortalRepository.PortalData.Failed -> {
                    _portalState.value = PortalState.Failed(categories.e.message + "test")
                    categories.e.cause?.printStackTrace()
                }

                is PortalRepository.PortalData.Success<List<PortalCategoryItem>> -> {
                    _portalCategoryList.value = categories.data
                }
            }
        }

        /**
         * 加载门户信息列表
         *
         */
        suspend fun loadPortalInfo() {
            val allInfos = mutableListOf<List<PortalInfoItem>>()
            portalCategoryList.value.forEach { category ->
                val infos = portalRepository.getPortalInfoList(category.portalID)
                when (infos) {
                    is PortalRepository.PortalData.Failed -> {
                        allInfos.add(emptyList())
                    }

                    is PortalRepository.PortalData.Success<List<PortalInfoItem>> -> {
                        val data = infos.data
                        allInfos.add(data)
                    }
                }
            }
            _portalInfoList.value = allInfos
            if (portalState.value == PortalState.Loading) {
                _portalState.value = PortalState.Success
            }
        }

        fun changePortalState(state: PortalState) {
            _portalState.value = state
        }

        suspend fun cleanPortal() {
            _portalInfoList.value = emptyList()
            _portalCategoryList.value = emptyList()
            portalRepository.cleanCache()
        }

        init {
            loadTimeTalk()
            viewModelScope.launch {
                _portalState.value = PortalState.Loading
                loadPortalCategories()
                loadPortalInfo()
            }
        }
    }
