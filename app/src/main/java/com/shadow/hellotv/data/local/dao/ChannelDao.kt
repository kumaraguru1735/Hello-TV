package com.shadow.hellotv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shadow.hellotv.data.local.entity.ChannelEntity

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY channelNo ASC")
    suspend fun getAll(): List<ChannelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun count(): Int
}
