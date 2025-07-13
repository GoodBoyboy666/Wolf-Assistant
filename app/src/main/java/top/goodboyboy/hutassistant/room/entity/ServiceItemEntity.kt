package top.goodboyboy.hutassistant.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_item")
data class ServiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUrl: String,
    val text: String,
    val serviceUrl: String,
    val tokenAccept: Int?,
)
