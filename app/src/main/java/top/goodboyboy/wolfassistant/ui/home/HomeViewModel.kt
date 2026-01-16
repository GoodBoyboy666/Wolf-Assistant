package top.goodboyboy.wolfassistant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalCategoryItem
import top.goodboyboy.wolfassistant.ui.home.portal.model.PortalInfoItem
import top.goodboyboy.wolfassistant.ui.home.portal.repository.PortalRepository
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
            _timeTalk.value =
                when (LocalTime.now().hour) {
                    in 5..6 -> "快要熬穿啦，赶紧去补补觉吧！"
                    in 7..10 -> "新的一天又开始了，祝你过得快乐!"
                    in 11..13 -> "该吃午饭啦！有什么好吃的？您有中午休息的好习惯吗？"
                    in 14..16 -> "下午好！外面的天气好吗？记得朵朵白云曾捎来朋友殷殷的祝福。"
                    in 17..18 -> "太阳落山了！快看看夕阳吧！如果外面下雨，就不必了 ^_^"
                    in 19..21 -> "晚上好，小伙伴今天的心情怎么样？"
                    in 22..23 -> "这么晚了，小伙伴还在上网？早点洗洗睡吧，睡前记得洗洗脸喔！"
                    in 0..1 -> "现在已经过凌晨了，身体是无价的资本喔，早点休息吧！"
                    in 2..4 -> "该休息了，身体可是革命的本钱啊！"
                    else -> "今天过得怎么样？"
                }
        }

        /**
         * 加载门户分类列表
         *
         */
        suspend fun loadPortalCategories() {
            val categories =
                portalRepository.getPortalCategory(
                    settingsRepository.getAccessTokenDecrypted(),
                )
            when (categories) {
                is PortalRepository.PortalData.Failed -> {
                    _portalState.value = PortalState.Failed(categories.e.message)
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
                val infos =
                    portalRepository.getPortalInfoList(
                        category.portalID,
                    )
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
