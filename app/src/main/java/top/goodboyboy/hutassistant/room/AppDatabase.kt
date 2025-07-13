package top.goodboyboy.hutassistant.room

import androidx.room.Database
import androidx.room.RoomDatabase
import top.goodboyboy.hutassistant.room.dao.ServiceItemDao
import top.goodboyboy.hutassistant.room.dao.TokenKeyNameDao
import top.goodboyboy.hutassistant.room.entity.ServiceItemEntity
import top.goodboyboy.hutassistant.room.entity.TokenKeyNameEntity

@Database(
    entities = [ServiceItemEntity::class, TokenKeyNameEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceItemDao(): ServiceItemDao

    abstract fun tokenKeyNameDao(): TokenKeyNameDao
}
