package com.shadow.hellotv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey val id: Int,
    val orderNo: Int,
    val name: String
)
