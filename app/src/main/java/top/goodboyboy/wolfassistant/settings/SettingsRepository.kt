package top.goodboyboy.wolfassistant.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val darkModeFlow: Flow<Boolean> =
            dataStore.data.map {
                it[booleanPreferencesKey("dark_mode")] ?: false
            }

        val userNameFlow: Flow<String> =
            dataStore.data.map {
                it[stringPreferencesKey("user_name")] ?: ""
            }

        val userIDFlow: Flow<String> =
            dataStore.data.map {
                it[stringPreferencesKey("user_id")] ?: ""
            }

        val userOrganization: Flow<String> =
            dataStore.data.map {
                it[stringPreferencesKey("user_organization")] ?: ""
            }

        val accessTokenFlow: Flow<String> =
            dataStore.data.map {
                it[stringPreferencesKey("access_token")] ?: ""
            }

        suspend fun setDarkMode(value: Boolean) {
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey("dark_mode")] = value
            }
        }

        suspend fun setUserName(value: String) {
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("user_name")] = value
            }
        }

        suspend fun setUserID(value: String) {
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("user_id")] = value
            }
        }

        suspend fun setAccessToken(value: String) {
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("access_token")] = value
            }
        }

        suspend fun setUserOrganization(value: String) {
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("user_organization")] = value
            }
        }

        suspend fun cleanAllData() {
            dataStore.edit { prefs ->
                prefs.clear()
            }
        }
    }
