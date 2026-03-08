package com.shadow.hellotv.data.repository

import com.shadow.hellotv.data.local.AppDatabase
import com.shadow.hellotv.data.local.entity.CategoryEntity
import com.shadow.hellotv.data.local.entity.ChannelEntity
import com.shadow.hellotv.data.local.entity.LanguageEntity
import com.shadow.hellotv.model.Category
import com.shadow.hellotv.model.Channel
import com.shadow.hellotv.model.Language
import com.shadow.hellotv.network.json
import kotlinx.serialization.json.JsonElement

class ChannelRepository(private val db: AppDatabase) {

    // ── Channel mapping ──

    private fun Channel.toEntity() = ChannelEntity(
        id = id, channelNo = channelNo, name = name, description = description,
        image = image, url = url, languageId = languageId, categoryId = categoryId,
        drmType = drmType, drmLicenceUrl = drmLicenceUrl,
        drmLicenceHeaders = drmLicenceHeaders?.let { json.encodeToString(JsonElement.serializer(), it) },
        premium = premium, price = price, streamType = streamType,
        headers = headers?.let { json.encodeToString(JsonElement.serializer(), it) }
    )

    private fun ChannelEntity.toModel() = Channel(
        id = id, channelNo = channelNo, name = name, description = description,
        image = image, url = url, languageId = languageId, categoryId = categoryId,
        drmType = drmType, drmLicenceUrl = drmLicenceUrl,
        drmLicenceHeaders = drmLicenceHeaders?.let { json.decodeFromString(JsonElement.serializer(), it) },
        premium = premium, price = price, streamType = streamType,
        headers = headers?.let { json.decodeFromString(JsonElement.serializer(), it) }
    )

    // ── Category mapping ──

    private fun Category.toEntity() = CategoryEntity(id = id, orderNo = orderNo, name = name)
    private fun CategoryEntity.toModel() = Category(id = id, orderNo = orderNo, name = name)

    // ── Language mapping ──

    private fun Language.toEntity() = LanguageEntity(id = id, orderNo = orderNo, name = name)
    private fun LanguageEntity.toModel() = Language(id = id, orderNo = orderNo, name = name)

    // ── Public API ──

    suspend fun saveChannels(channels: List<Channel>) {
        db.channelDao().deleteAll()
        db.channelDao().insertAll(channels.map { it.toEntity() })
    }

    suspend fun getChannels(): List<Channel> =
        db.channelDao().getAll().map { it.toModel() }

    suspend fun hasChannels(): Boolean = db.channelDao().count() > 0

    suspend fun saveCategories(categories: List<Category>) {
        db.categoryDao().deleteAll()
        db.categoryDao().insertAll(categories.map { it.toEntity() })
    }

    suspend fun getCategories(): List<Category> =
        db.categoryDao().getAll().map { it.toModel() }

    suspend fun saveLanguages(languages: List<Language>) {
        db.languageDao().deleteAll()
        db.languageDao().insertAll(languages.map { it.toEntity() })
    }

    suspend fun getLanguages(): List<Language> =
        db.languageDao().getAll().map { it.toModel() }

    suspend fun clearAll() {
        db.channelDao().deleteAll()
        db.categoryDao().deleteAll()
        db.languageDao().deleteAll()
    }
}
