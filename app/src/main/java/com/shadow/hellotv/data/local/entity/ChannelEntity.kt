package com.shadow.hellotv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: Int,
    val channelNo: Int,
    val name: String,
    val description: String,
    val image: String,
    val url: String,
    val languageId: Int,
    val categoryId: Int,
    val drmType: String?,
    val drmLicenceUrl: String?,
    val drmLicenceHeaders: String?,
    val premium: Int,
    val price: Double,
    val streamType: String,
    val headers: String?
)
