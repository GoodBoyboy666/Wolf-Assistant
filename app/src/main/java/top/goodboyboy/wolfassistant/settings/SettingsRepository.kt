package top.goodboyboy.wolfassistant.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.util.CryptoManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 设置仓库，用于管理应用的所有持久化配置数据。
 * 基于 DataStore 实现。
 */
@Singleton
class SettingsRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
        private val cryptoManager: CryptoManager,
        private val logger: AppLogger,
    ) {
        /**
         * 深色模式状态流
         */
        val darkModeFlow: Flow<Boolean> =
            dataStore.data.map {
                it[booleanPreferencesKey("dark_mode")] ?: false
            }

        /**
         * 用户名称流
         */
        val userNameFlow: Flow<String> =
            dataStore.data.map {
                it[stringPreferencesKey("user_name")] ?: ""
            }

        /**
         * 用户 ID 流
         */
        val userIDFlow: Flow<String> =
            dataStore.data.map {
                it[stringPreferencesKey("user_id")] ?: ""
            }

//        /**
//         * 用户密码流
//         */
//        val userPasswdFlow: Flow<String> =
//            dataStore.data.map {
//                it[stringPreferencesKey("user_passwd")] ?: ""
//            }

        /**
         * 用户组织流
         */
        val userOrganization: Flow<String> =
            dataStore.data.map {
                it[stringPreferencesKey("user_organization")] ?: ""
            }

//        /**
//         * 访问令牌流
//         */
//        val accessTokenFlow: Flow<String> =
//            dataStore.data.map {
//                it[stringPreferencesKey("access_token")] ?: ""
//            }

        /**
         * 禁用 SSL 证书验证状态流
         */
        val disableSSLCertVerification: Flow<Boolean> =
            dataStore.data.map {
                it[booleanPreferencesKey("disable_SSLCert_verification")] ?: false
            }

        /**
         * 仅使用 IPv4 状态流
         */
        val onlyIPv4: Flow<Boolean> =
            dataStore.data.map {
                it[booleanPreferencesKey("only_IPv4")] ?: false
            }

        /**
         * 选择的周次
         */
        val selectWeekNum: Flow<Int> =
            dataStore.data.map {
                val weekStr = it[stringPreferencesKey("select_week_num")] ?: "1"
                weekStr.toIntOrNull() ?: 1
            }

        /**
         * 启用预发布版本
         */
        val enablePreRelease: Flow<Boolean> =
            dataStore.data.map {
                it[booleanPreferencesKey("enable_pre_release")] ?: false
            }

        /**
         * 设置深色模式
         * @param value true 开启，false 关闭
         */
        suspend fun setDarkMode(value: Boolean) {
            logger.tag("Settings").i("设置深色模式")
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey("dark_mode")] = value
            }
        }

        /**
         * 设置用户名称
         * @param value 用户名称
         */
        suspend fun setUserName(value: String) {
            logger.tag("Settings").i("更新用户名")
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("user_name")] = value
            }
        }

        /**
         * 设置用户 ID
         * @param value 用户 ID
         */
        suspend fun setUserID(value: String) {
            logger.tag("Settings").i("更新用户ID")
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("user_id")] = value
            }
        }

//        /**
//         * 设置用户密码
//         * @param value 用户密码
//         */
//        suspend fun setUserPasswd(value: String) {
//            dataStore.edit { prefs ->
//                prefs[stringPreferencesKey("user_passwd")] = value
//            }
//        }

//        /**
//         * 设置访问令牌
//         * @param value Access Token
//         */
//        suspend fun setAccessToken(value: String) {
//            dataStore.edit { prefs ->
//                prefs[stringPreferencesKey("access_token")] = value
//            }
//        }

        /**
         * 设置用户组织
         * @param value 组织名称
         */
        suspend fun setUserOrganization(value: String) {
            logger.tag("Settings").i("更新用户组织")
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("user_organization")] = value
            }
        }

        /**
         * 设置选择的周次
         * @param value 周次数字
         */
        suspend fun setSelectWeekNum(value: Int) {
            logger.tag("Settings").i("设置选择周次")
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("select_week_num")] = value.toString()
            }
        }

        /**
         * 设置是否启用预发布版本更新
         * @param value true 启用，false 禁用
         */
        suspend fun setEnablePreRelease(value: Boolean) {
            logger.tag("Settings").i("设置预发布版本更新")
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey("enable_pre_release")] = value
            }
        }

        /**
         * 清除所有数据
         */
        suspend fun cleanAllData() {
            logger.tag("Settings").i("清除全部设置数据")
            dataStore.edit { prefs ->
                prefs.clear()
            }
        }

        /**
         * 清除用户相关数据 (用户名、ID、Token、组织)
         */
        suspend fun cleanUser() {
            logger.tag("Settings").i("清除用户数据")
            dataStore.edit { prefs ->
                prefs.remove(stringPreferencesKey("user_name"))
                prefs.remove(stringPreferencesKey("user_id"))
                prefs.remove(stringPreferencesKey("access_token"))
                prefs.remove(stringPreferencesKey("user_organization"))
            }
        }

        /**
         * 设置是否禁用 SSL 证书验证
         * @param value true 禁用，false 启用
         */
        suspend fun setSSLCertVerification(value: Boolean) {
            logger.tag("Settings").i("设置SSL证书验证")
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey("disable_SSLCert_verification")] = value
            }
        }

        /**
         * 设置是否仅使用 IPv4
         * @param value true 仅 IPv4，false 不限制
         */
        suspend fun setOnlyIPv4(value: Boolean) {
            logger.tag("Settings").i("设置仅IPv4")
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey("only_IPv4")] = value
            }
        }

        suspend fun setUserPasswordEncrypted(password: String) {
            require(password.isNotEmpty()) {
                "Password cannot be empty"
            }
            logger.tag("Settings").i("保存加密密码")
            val encryptedPassword = cryptoManager.encrypt(password)
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("encrypted_user_passwd")] = encryptedPassword
            }
        }

        suspend fun setAccessTokenEncrypted(accessToken: String) {
            require(accessToken.isNotEmpty()) {
                "Access token cannot be empty"
            }
            logger.tag("Settings").i("保存加密令牌")
            val encryptedToken = cryptoManager.encrypt(accessToken)
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("encrypted_access_token")] = encryptedToken
            }
        }

        suspend fun getUserPasswordDecrypted(): String {
            logger.tag("Settings").i("读取解密密码")
            val encryptedPassword =
                dataStore.data
                    .map {
                        it[stringPreferencesKey("encrypted_user_passwd")] ?: ""
                    }.first()
            if (encryptedPassword.isEmpty()) {
                return ""
            }
            return cryptoManager.decrypt(encryptedPassword)
        }

        suspend fun getAccessTokenDecrypted(): String {
            logger.tag("Settings").i("读取解密令牌")
            val encryptedToken =
                dataStore.data
                    .map {
                        it[stringPreferencesKey("encrypted_access_token")] ?: ""
                    }.first()
            if (encryptedToken.isEmpty()) {
                return ""
            }
            return cryptoManager.decrypt(encryptedToken)
        }
    }
