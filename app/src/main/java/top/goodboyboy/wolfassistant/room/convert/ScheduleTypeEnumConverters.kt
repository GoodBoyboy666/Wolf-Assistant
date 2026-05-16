package top.goodboyboy.wolfassistant.room.convert

import androidx.room.TypeConverter
import top.goodboyboy.wolfassistant.room.const.ScheduleType

class ScheduleTypeEnumConverters {
    @TypeConverter
    fun fromScheduleType(type: ScheduleType): String {
        return type.name // 将枚举转换为对应的字符串
    }

    @TypeConverter
    fun toScheduleType(typeString: String): ScheduleType =
        try {
            ScheduleType.valueOf(typeString) // 将字符串还原为枚举
        } catch (e: IllegalArgumentException) {
            ScheduleType.UNKNOWN
        }
}
