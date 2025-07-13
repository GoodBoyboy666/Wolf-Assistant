package top.goodboyboy.hutassistant.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import top.goodboyboy.hutassistant.room.entity.TokenKeyNameEntity

@Dao
interface TokenKeyNameDao {
    @Insert
    suspend fun insert(tokenKeyNameEntity: TokenKeyNameEntity): Long

    @Delete
    suspend fun delete(tokenKeyNameEntity: TokenKeyNameEntity)

    @Query("DELETE FROM token_key_name WHERE id = :id")
    suspend fun deleteByID(id: Int)

    @Query("DELETE FROM token_key_name")
    suspend fun cleanTokenKeyName()

    @Query("SELECT * FROM token_key_name WHERE id = :id")
    suspend fun getData(id: Int): TokenKeyNameEntity?

    @Update
    suspend fun update(tokenKeyNameEntity: TokenKeyNameEntity)

    @Query("SELECT * FROM token_key_name")
    fun getAllTokenKeyName(): Flow<List<TokenKeyNameEntity>>

    @Query("SELECT * FROM token_key_name")
    suspend fun getAllTokenKeyNameEntity(): List<TokenKeyNameEntity>
}
