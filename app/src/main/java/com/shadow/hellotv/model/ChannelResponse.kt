package com.shadow.hellotv.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChannelResponse(
    val success: Boolean,
    val message: String,
    val data: List<ChannelItem>
)

@Serializable
data class ChannelItem(
    val id: Int,
    val name: String,
    val logo: String,
    val category: String,
    val url: String,
    @SerialName("player_headers")
    val playerHeaders: JsonElement? = null,  // ← can hold String, Object, or Array
    @SerialName("drm_url")
    val drmUrl: String? = null,
    @SerialName("drm_headers")
    val drmHeaders: JsonElement? = null,     // optional same flexibility
    @SerialName("drm_type")
    val drmType: DRMType? = null,
)

enum class DRMType{
    CLEARKEY,
    WIDEVINE,
    PLAYREADY,
    FAIRPLAY
}