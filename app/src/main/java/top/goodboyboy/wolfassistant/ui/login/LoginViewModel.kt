package top.goodboyboy.wolfassistant.ui.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.goodboyboy.wolfassistant.settings.SettingsRepository
import top.goodboyboy.wolfassistant.ui.login.repository.LoginRepository
import top.goodboyboy.wolfassistant.util.Hash.sha256
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val loginRepository: LoginRepository,
    ) : ViewModel() {
        sealed class LoginState {
            object Idle : LoginState()

            object Loading : LoginState()

            object Success : LoginState()

            data class Failed(
                val message: String,
            ) : LoginState()
        }

        private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
        val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

        /**
         * 登录
         *
         * @param id 学号
         * @param passwd 密码
         */
        suspend fun login(
            id: String,
            passwd: String,
        ) {
            _loginState.value = LoginState.Loading
            val status =
                loginRepository.loginUser(
                    username = id,
                    password = passwd,
                    appId = "com.supwisdom.hut",
                    deviceId = sha256(Random.nextInt(10000000).toString()),
                    osType = "Android",
                    clientId = sha256(Random.nextInt(10000000).toString()),
                )
            when (status) {
                is LoginRepository.UserData.Failed -> {
                    _loginState.value = LoginState.Failed(status.error.message)
                }

                is LoginRepository.UserData.Success -> {
                    settingsRepository.setUserID(status.data.userID)
                    settingsRepository.setUserOrganization(status.data.userOrganization)
                    settingsRepository.setUserName(status.data.userName)
                    settingsRepository.setAccessToken(status.data.accessToken)
                    _loginState.value = LoginState.Success
                }
            }
        }
    }
