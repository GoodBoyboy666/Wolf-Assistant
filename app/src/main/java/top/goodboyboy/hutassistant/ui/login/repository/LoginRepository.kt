package top.goodboyboy.hutassistant.ui.login.repository

import top.goodboyboy.hutassistant.common.Failure
import top.goodboyboy.hutassistant.ui.login.model.UserInfo

interface LoginRepository {
    /**
     * 登录用户
     *
     * @param username 学号
     * @param password 密码
     * @param appId appid
     * @param deviceId 设备id
     * @param osType 系统类型
     * @param clientId 客户端id
     * @return UserData
     */
    suspend fun loginUser(
        username: String,
        password: String,
        appId: String,
        deviceId: String,
        osType: String,
        clientId: String,
    ): UserData

    sealed class UserData {
        data class Success(
            val data: UserInfo,
        ) : UserData()

        data class Failed(
            val error: Failure,
        ) : UserData()
    }
}
