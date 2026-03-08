package com.shadow.hellotv.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── Unified API Response wrapper ──
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    @SerialName("error_code")
    val errorCode: String? = null,
    val timestamp: String? = null,
    val data: T? = null
)

// ── Login ──
@Serializable
data class LoginRequest(
    val phone: String,
    val pin: String,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("device_type")
    val deviceType: String,
    @SerialName("device_model")
    val deviceModel: String = "",
    @SerialName("device_name")
    val deviceName: String = ""
)

@Serializable
data class LoginData(
    val subscriber: Subscriber,
    @SerialName("session_token")
    val sessionToken: String,
    @SerialName("session_type")
    val sessionType: String,
    @SerialName("device_limits")
    val deviceLimits: DeviceLimits? = null,
    @SerialName("current_sessions")
    val currentSessions: List<SessionInfo>? = null
)

@Serializable
data class Subscriber(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String = "",
    @SerialName("expired_at")
    val expiredAt: String? = null
)

@Serializable
data class DeviceLimits(
    @SerialName("android_mobile")
    val androidMobile: Int = 1,
    @SerialName("android_tv")
    val androidTv: Int = 1
)

@Serializable
data class SessionInfo(
    val id: Int,
    @SerialName("device_id")
    val deviceId: String = "",
    @SerialName("device_name")
    val deviceName: String = "",
    @SerialName("device_model")
    val deviceModel: String = "",
    @SerialName("device_type")
    val deviceType: String = "",
    @SerialName("ip_address")
    val ipAddress: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("last_login")
    val lastLogin: String = ""
)

// ── Validate Session ──
@Serializable
data class ValidateData(
    val subscriber: Subscriber,
    @SerialName("session_info")
    val sessionInfo: SessionDetail? = null,
    val sessions: List<SessionInfo>? = null
)

@Serializable
data class SessionDetail(
    @SerialName("device_id")
    val deviceId: String = "",
    @SerialName("device_name")
    val deviceName: String = "",
    @SerialName("device_model")
    val deviceModel: String = "",
    @SerialName("device_type")
    val deviceType: String = "",
    @SerialName("session_created")
    val sessionCreated: String = "",
    @SerialName("last_login")
    val lastLogin: String = "",
    @SerialName("session_type")
    val sessionType: String = ""
)

// ── Sessions List ──
@Serializable
data class SessionsData(
    val sessions: List<SessionInfo> = emptyList(),
    @SerialName("current_session_type")
    val currentSessionType: String = "",
    @SerialName("total_sessions")
    val totalSessions: Int = 0
)

// ── Channels ──
@Serializable
data class ChannelsData(
    val subscriber: Subscriber? = null,
    @SerialName("session_info")
    val sessionInfo: SessionDetail? = null,
    @SerialName("subscription_info")
    val subscriptionInfo: SubscriptionInfo? = null,
    val channels: List<Channel> = emptyList(),
    @SerialName("total_channels")
    val totalChannels: Int = 0,
    @SerialName("allowed_channels_count")
    val allowedChannelsCount: Int = 0
)

@Serializable
data class SubscriptionInfo(
    @SerialName("subscription_expired")
    val subscriptionExpired: Boolean = false,
    @SerialName("expired_at")
    val expiredAt: String? = null,
    @SerialName("expired_in")
    val expiredIn: String? = null,
    val status: String = ""
)

@Serializable
data class Channel(
    val id: Int,
    @SerialName("channel_no")
    val channelNo: Int = 0,
    val name: String,
    val description: String = "",
    val image: String = "",
    val url: String,
    @SerialName("language_id")
    val languageId: Int = 0,
    @SerialName("category_id")
    val categoryId: Int = 0,
    @SerialName("drm_type")
    val drmType: String? = null,
    @SerialName("drm_licence_url")
    val drmLicenceUrl: String? = null,
    @SerialName("drm_licence_headers")
    val drmLicenceHeaders: JsonElement? = null,
    val premium: Int = 0,
    val price: Double = 0.0,
    @SerialName("stream_type")
    val streamType: String = "hls",
    val headers: JsonElement? = null
)

// ── Categories ──
@Serializable
data class CategoriesData(
    val subscriber: Subscriber? = null,
    @SerialName("session_info")
    val sessionInfo: SessionDetail? = null,
    val categories: List<Category> = emptyList(),
    @SerialName("total_categories")
    val totalCategories: Int = 0
)

@Serializable
data class Category(
    val id: Int,
    @SerialName("order_no")
    val orderNo: Int = 0,
    val name: String
)

// ── Languages ──
@Serializable
data class LanguagesData(
    val subscriber: Subscriber? = null,
    @SerialName("session_info")
    val sessionInfo: SessionDetail? = null,
    val languages: List<Language> = emptyList(),
    @SerialName("total_languages")
    val totalLanguages: Int = 0
)

@Serializable
data class Language(
    val id: Int,
    @SerialName("order_no")
    val orderNo: Int = 0,
    val name: String
)

// ── Kick / Replace Device ──
@Serializable
data class KickData(
    @SerialName("kicked_session_id")
    val kickedSessionId: Int = 0
)

@Serializable
data class ReplaceData(
    val subscriber: Subscriber? = null,
    @SerialName("kicked_session_id")
    val kickedSessionId: Int = 0,
    @SerialName("session_token")
    val sessionToken: String = "",
    @SerialName("session_type")
    val sessionType: String = "",
    @SerialName("device_limits")
    val deviceLimits: DeviceLimits? = null
)
