package top.goodboyboy.wolfassistant.ui.servicecenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.model.ServiceItem
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.SearchRepository
import top.goodboyboy.wolfassistant.ui.servicecenter.service.repository.ServiceRepository
import javax.inject.Inject

@HiltViewModel
class ServiceCenterViewModel
    @Inject
    constructor(
        private val serviceRepository: ServiceRepository,
        private val settingsRepository: SettingsRepository,
        private val searchRepository: SearchRepository,
        val okHttpClient: OkHttpClient,
        private val logger: AppLogger,
    ) : ViewModel() {
        private val _loadServiceState = MutableStateFlow<LoadServiceState>(LoadServiceState.Idle)
        val loadServiceState: StateFlow<LoadServiceState> = _loadServiceState.asStateFlow()

        private val allServiceItems = MutableStateFlow<List<ServiceItem>>(emptyList())

        val searchQuery: StateFlow<String> = searchRepository.searchQuery

        @OptIn(FlowPreview::class)
        val serviceList: StateFlow<List<ServiceItem>> =
            combine(
                allServiceItems,
                searchRepository.searchQuery.debounce(100),
            ) { allItems, query ->
                if (query.isBlank()) {
                    allItems
                } else {
                    allItems.filter {
                        it.text.contains(query, ignoreCase = true)
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = allServiceItems.value,
            )

        sealed class LoadServiceState {
            object Idle : LoadServiceState()

            object Loading : LoadServiceState()

            object Success : LoadServiceState()

            data class Failed(
                val message: String,
            ) : LoadServiceState()
        }

        suspend fun loadService() {
            logger.i("加载服务列表")
            _loadServiceState.value = LoadServiceState.Loading
            val accessToken = settingsRepository.getAccessTokenDecrypted()
            val data =
                serviceRepository.getServiceList(
                    accessToken,
                )
            when (data) {
                is ServiceRepository.ServiceListData.Failed -> {
                    _loadServiceState.value = LoadServiceState.Failed(data.error.message)
                    logger.e(data.error.cause, "加载服务列表失败")
                }

                is ServiceRepository.ServiceListData.Success -> {
                    logger.i("加载服务列表成功")
                    allServiceItems.value = data.data
                    _loadServiceState.value = LoadServiceState.Success
                }
            }
        }

        suspend fun cleanServiceList() {
            logger.i("清除服务列表")
            allServiceItems.value = emptyList()
            serviceRepository.cleanServiceList()
        }

        suspend fun updateQuery(query: String) {
            searchRepository.updateQuery(query)
        }

        init {
            viewModelScope.launch {
                loadService()
            }
        }
    }
