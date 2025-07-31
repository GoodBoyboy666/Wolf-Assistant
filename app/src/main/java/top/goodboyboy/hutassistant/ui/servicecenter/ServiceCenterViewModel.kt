package top.goodboyboy.hutassistant.ui.servicecenter

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import top.goodboyboy.hutassistant.settings.SettingsRepository
import top.goodboyboy.hutassistant.ui.servicecenter.service.model.ServiceItem
import top.goodboyboy.hutassistant.ui.servicecenter.service.repository.ServiceRepository
import javax.inject.Inject

@HiltViewModel
class ServiceCenterViewModel
    @Inject
    constructor(
        private val serviceRepository: ServiceRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _loadServiceState = MutableStateFlow<LoadServiceState>(LoadServiceState.Idle)
        val loadServiceState: StateFlow<LoadServiceState> = _loadServiceState.asStateFlow()
        private val _serviceList = MutableStateFlow<List<ServiceItem>>(emptyList())
        val serviceList: StateFlow<List<ServiceItem>> = _serviceList.asStateFlow()

        sealed class LoadServiceState {
            object Idle : LoadServiceState()

            object Loading : LoadServiceState()

            object Success : LoadServiceState()

            data class Failed(
                val message: String,
            ) : LoadServiceState()
        }

        init {
//            viewModelScope.launch {
//                _loadServiceState.value = LoadServiceState.Loading
//                loadService()
//            }
        }

        suspend fun loadService() {
            val accessToken = settingsRepository.accessTokenFlow.first()
            val data = serviceRepository.getServiceList(accessToken)
            when (data) {
                is ServiceRepository.ServiceListData.Failed -> {
                    _loadServiceState.value = LoadServiceState.Failed(data.error.message)
                    data.error.cause?.printStackTrace()
                }

                is ServiceRepository.ServiceListData.Success -> {
                    _serviceList.value = data.data
                    _loadServiceState.value = LoadServiceState.Success
                }
            }
        }

        suspend fun cleanServiceList() {
            _serviceList.value = emptyList()
            serviceRepository.cleanServiceList()
        }

        fun changeLoadServiceState(state: LoadServiceState) {
            _loadServiceState.value = state
        }
    }
