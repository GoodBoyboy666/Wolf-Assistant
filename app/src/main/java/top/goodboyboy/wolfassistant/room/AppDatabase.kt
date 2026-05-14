package top.goodboyboy.wolfassistant.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import top.goodboyboy.wolfassistant.room.convert.DateTimeConverters
import top.goodboyboy.wolfassistant.room.convert.ScheduleTypeEnumConverters
import top.goodboyboy.wolfassistant.room.dao.ScheduleNotificationTaskDao
import top.goodboyboy.wolfassistant.room.dao.ServiceItemDao
import top.goodboyboy.wolfassistant.room.dao.TokenKeyNameDao
import top.goodboyboy.wolfassistant.room.entity.ScheduleNotificationTaskEntity
import top.goodboyboy.wolfassistant.room.entity.ServiceItemEntity
import top.goodboyboy.wolfassistant.room.entity.TokenKeyNameEntity

@Database(
    entities = [ServiceItemEntity::class, TokenKeyNameEntity::class, ScheduleNotificationTaskEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true,
)
@TypeConverters(DateTimeConverters::class, ScheduleTypeEnumConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceItemDao(): ServiceItemDao

    abstract fun tokenKeyNameDao(): TokenKeyNameDao

    abstract fun scheduleNotificationTaskDao(): ScheduleNotificationTaskDao
}
