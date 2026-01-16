package top.goodboyboy.wolfassistant.ui.personalcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.model.PersonalInfo
import top.goodboyboy.wolfassistant.ui.personalcenter.personal.repository.PersonalInfoRepository
import javax.inject.Inject

@HiltViewModel
class PersonalCenterViewModel
    @Inject
    constructor(
        private val personalInfoRepository: PersonalInfoRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
        val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

        private val _personalInfo = MutableStateFlow<PersonalInfo?>(null)
        val personalInfo: StateFlow<PersonalInfo?> = _personalInfo.asStateFlow()

        sealed class LoadState {
            object Idle : LoadState()

            object Loading : LoadState()

            object Success : LoadState()

            data class Failed(
                val reason: String,
            ) : LoadState()
        }

        init {
            viewModelScope.launch {
                _loadState.value = LoadState.Loading
                loadPersonalInfo()
            }
        }

        suspend fun loadPersonalInfo() {
            val accessToken = settingsRepository.getAccessTokenDecrypted()
            val info =
                personalInfoRepository.getPersonalInfo(
                    accessToken,
                )
            when (info) {
                is PersonalInfoRepository.PersonalInfoData.Failed -> {
                    withContext(Dispatchers.Main) {
                        _loadState.value = LoadState.Failed(info.error.message)
                        info.error.cause?.printStackTrace()
                    }
                }

                is PersonalInfoRepository.PersonalInfoData.Success -> {
                    withContext(Dispatchers.Main) {
                        _personalInfo.value = info.data
                        _loadState.value = LoadState.Success
                    }
                }
            }
        }
    }
