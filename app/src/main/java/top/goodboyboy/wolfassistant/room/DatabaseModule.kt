package top.goodboyboy.wolfassistant.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.goodboyboy.wolfassistant.room.dao.ServiceItemDao
import top.goodboyboy.wolfassistant.room.dao.TokenKeyNameDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder<AppDatabase>(
                context,
                "app_database",
            ).build()

    @Provides
    fun provideServiceItemDao(database: AppDatabase): ServiceItemDao = database.serviceItemDao()

    @Provides
    fun provideTokenKeyNameDao(database: AppDatabase): TokenKeyNameDao = database.tokenKeyNameDao()
}
