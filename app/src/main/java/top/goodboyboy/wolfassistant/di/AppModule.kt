package top.goodboyboy.wolfassistant.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import top.goodboyboy.wolfassistant.common.GlobalEventBus
import top.goodboyboy.wolfassistant.log.AppLogger
import top.goodboyboy.wolfassistant.settings.migration.PlainPasswdAndAKToEncryptedMigration
import top.goodboyboy.wolfassistant.util.CryptoManager
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context,
    ): Context = context

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("app_settings") },
            migrations = listOf(PlainPasswdAndAKToEncryptedMigration(cryptoManager)),
        )

    @Provides
    @Singleton
    fun provideCryptoManager(logger: AppLogger): CryptoManager = CryptoManager(logger)

    @Provides
    @Singleton
    fun provideGlobalEventBus(logger: AppLogger): GlobalEventBus = GlobalEventBus(logger)

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ApplicationScope

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        // SupervisorJob() 避免取消整个作用域
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
