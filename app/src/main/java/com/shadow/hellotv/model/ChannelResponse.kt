package com.shadow.hellotv.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val playerHeaders: String? = null,
    @SerialName("drm_url")
    val drmUrl: String? = null,
    @SerialName("drm_headers")
    val drmHeaders: String? = null,
    val cookie: String? = null,
    @SerialName("user_agent")
    val userAgent: String? = null,
    val referer: String? = null,
)
