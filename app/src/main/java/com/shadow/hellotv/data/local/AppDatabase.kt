package com.shadow.hellotv.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shadow.hellotv.data.local.dao.CategoryDao
import com.shadow.hellotv.data.local.dao.ChannelDao
import com.shadow.hellotv.data.local.dao.LanguageDao
import com.shadow.hellotv.data.local.entity.CategoryEntity
import com.shadow.hellotv.data.local.entity.ChannelEntity
import com.shadow.hellotv.data.local.entity.LanguageEntity

@Database(
    entities = [ChannelEntity::class, CategoryEntity::class, LanguageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun categoryDao(): CategoryDao
    abstract fun languageDao(): LanguageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hellotv.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
