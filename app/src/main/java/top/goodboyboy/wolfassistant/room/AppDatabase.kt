package top.goodboyboy.wolfassistant.room

import androidx.room.Database
import androidx.room.RoomDatabase
import top.goodboyboy.wolfassistant.room.dao.ServiceItemDao
import top.goodboyboy.wolfassistant.room.dao.TokenKeyNameDao
import top.goodboyboy.wolfassistant.room.entity.ServiceItemEntity
import top.goodboyboy.wolfassistant.room.entity.TokenKeyNameEntity

@Database(
    entities = [ServiceItemEntity::class, TokenKeyNameEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceItemDao(): ServiceItemDao

    abstract fun tokenKeyNameDao(): TokenKeyNameDao
}
