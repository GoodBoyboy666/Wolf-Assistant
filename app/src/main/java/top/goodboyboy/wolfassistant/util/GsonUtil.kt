package top.goodboyboy.wolfassistant.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * 创建支持日期序列化的Gson实例
 *
 * @constructor Create empty Gson util
 */
object GsonUtil {
    fun getGson(): Gson =
        GsonBuilder()
            .registerTypeAdapter(
                LocalDateTime::class.java,
                JsonSerializer<LocalDateTime> { src, _, _ ->
                    JsonPrimitive(src?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                },
            ).registerTypeAdapter(
                LocalDateTime::class.java,
                JsonDeserializer { json, _, _ ->
                    LocalDateTime.parse(json?.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                },
            ).registerTypeAdapter(
                LocalDate::class.java,
                JsonSerializer<LocalDate> { src, _, _ ->
                    JsonPrimitive(src?.format(DateTimeFormatter.ISO_LOCAL_DATE))
                },
            ).registerTypeAdapter(
                LocalDate::class.java,
                JsonDeserializer { json, _, _ ->
                    LocalDate.parse(json?.asString, DateTimeFormatter.ISO_LOCAL_DATE)
                },
            ).registerTypeAdapter(
                LocalTime::class.java,
                JsonSerializer<LocalTime> { src, _, _ ->
                    JsonPrimitive(src?.format(DateTimeFormatter.ISO_LOCAL_TIME))
                },
            ).registerTypeAdapter(
                LocalTime::class.java,
                JsonDeserializer { json, _, _ ->
                    LocalTime.parse(json?.asString, DateTimeFormatter.ISO_LOCAL_TIME)
                },
            ).registerTypeAdapter(
                OffsetDateTime::class.java,
                JsonSerializer<OffsetDateTime> { src, _, _ ->
                    JsonPrimitive(src?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                },
            ).registerTypeAdapter(
                OffsetDateTime::class.java,
                JsonDeserializer { json, _, _ ->
                    OffsetDateTime.parse(json?.asString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                },
            ).create()
}
