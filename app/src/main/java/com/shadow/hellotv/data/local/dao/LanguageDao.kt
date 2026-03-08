package com.shadow.hellotv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shadow.hellotv.data.local.entity.LanguageEntity

@Dao
interface LanguageDao {
    @Query("SELECT * FROM languages ORDER BY orderNo ASC")
    suspend fun getAll(): List<LanguageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(languages: List<LanguageEntity>)

    @Query("DELETE FROM languages")
    suspend fun deleteAll()
}
