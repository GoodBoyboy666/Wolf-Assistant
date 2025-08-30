package top.goodboyboy.wolfassistant.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import top.goodboyboy.wolfassistant.room.entity.ServiceItemEntity

@Dao
interface ServiceItemDao {
    @Insert
    suspend fun insert(serviceItemEntity: ServiceItemEntity): Long

    @Delete
    suspend fun delete(serviceItemEntity: ServiceItemEntity)

    @Query("DELETE FROM service_item WHERE id = :id")
    suspend fun deleteByID(id: Int)

    @Query("DELETE FROM service_item")
    suspend fun cleanServiceList()

    @Update
    suspend fun update(serviceItemEntity: ServiceItemEntity)

    @Query("SELECT * FROM service_item")
    fun getAllServiceItem(): Flow<List<ServiceItemEntity>>

    @Query("SELECT * FROM service_item")
    suspend fun getAllServiceItemEntity(): List<ServiceItemEntity>
}
