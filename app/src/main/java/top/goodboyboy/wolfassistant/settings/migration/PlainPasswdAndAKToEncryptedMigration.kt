package top.goodboyboy.wolfassistant.settings.migration

import android.util.Log
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import top.goodboyboy.wolfassistant.util.CryptoManager

val KEY_PLAIN_PASSWORD = stringPreferencesKey("user_passwd")
val KEY_ENCRYPTED_PASSWORD = stringPreferencesKey("encrypted_user_passwd")
val KEY_PLAIN_ACCESS_TOKEN = stringPreferencesKey("access_token")
val KEY_ENCRYPTED_ACCESS_TOKEN = stringPreferencesKey("encrypted_access_token")

class PlainPasswdAndAKToEncryptedMigration(
    val cryptoManager: CryptoManager,
) : DataMigration<Preferences> {
    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        // 如果没有明文密码，说明已经完成迁移，不进行迁移
        // 或者说明用户从老旧版本的APP直接升级，需要重新登录
        return currentData.contains(KEY_PLAIN_PASSWORD)
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        Log.i("SettingsMigration", "Migrating plain password and access token to encrypted versions")
        val plainPassword = currentData[KEY_PLAIN_PASSWORD]
        val plainAccessToken = currentData[KEY_PLAIN_ACCESS_TOKEN]
        if (plainPassword == null || plainAccessToken == null) {
            return currentData
        }

        val encryptedPassword = cryptoManager.encrypt(plainPassword)
        val encryptedAccessToken = cryptoManager.encrypt(plainAccessToken)

        val mutablePreferences = currentData.toMutablePreferences()

        mutablePreferences[KEY_ENCRYPTED_PASSWORD] = encryptedPassword
        mutablePreferences.remove(KEY_PLAIN_PASSWORD)

        mutablePreferences[KEY_ENCRYPTED_ACCESS_TOKEN] = encryptedAccessToken
        mutablePreferences.remove(KEY_PLAIN_ACCESS_TOKEN)

        return mutablePreferences
    }

    override suspend fun cleanUp() = Unit
}
