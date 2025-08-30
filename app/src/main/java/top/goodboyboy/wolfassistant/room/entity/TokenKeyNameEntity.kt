package top.goodboyboy.wolfassistant.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_key_name")
data class TokenKeyNameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val headerTokenKeyName: String?,
    val urlTokenKeyName: String?,
)
