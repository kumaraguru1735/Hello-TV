package com.shadow.hellotv.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import java.util.UUID

class SessionManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hellotv_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_SESSION_TYPE = "session_type"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_EXPIRED_AT = "expired_at"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SAVED_PHONE = "saved_phone"
        private const val KEY_SAVED_PIN = "saved_pin"
    }

    var sessionToken: String?
        get() = prefs.getString(KEY_SESSION_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_SESSION_TOKEN, value).apply()

    var sessionType: String?
        get() = prefs.getString(KEY_SESSION_TYPE, null)
        set(value) = prefs.edit().putString(KEY_SESSION_TYPE, value).apply()

    var userId: Int
        get() = prefs.getInt(KEY_USER_ID, 0)
        set(value) = prefs.edit().putInt(KEY_USER_ID, value).apply()

    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userPhone: String?
        get() = prefs.getString(KEY_USER_PHONE, null)
        set(value) = prefs.edit().putString(KEY_USER_PHONE, value).apply()

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var expiredAt: String?
        get() = prefs.getString(KEY_EXPIRED_AT, null)
        set(value) = prefs.edit().putString(KEY_EXPIRED_AT, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var rememberMe: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_ME, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply()

    var savedPhone: String?
        get() = prefs.getString(KEY_SAVED_PHONE, null)
        set(value) = prefs.edit().putString(KEY_SAVED_PHONE, value).apply()

    var savedPin: String?
        get() = prefs.getString(KEY_SAVED_PIN, null)
        set(value) = prefs.edit().putString(KEY_SAVED_PIN, value).apply()

    fun saveCredentials(phone: String, pin: String) {
        savedPhone = phone
        savedPin = pin
        rememberMe = true
    }

    fun clearCredentials() {
        savedPhone = null
        savedPin = null
        rememberMe = false
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    fun getDeviceType(): String {
        return if (context.packageManager.hasSystemFeature("android.software.leanback")) {
            "android_tv"
        } else {
            "android_mobile"
        }
    }

    fun getDeviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}"

    fun getDeviceName(): String = Build.MODEL

    fun saveLoginData(
        sessionToken: String,
        sessionType: String,
        userId: Int,
        userName: String,
        userPhone: String,
        userEmail: String,
        expiredAt: String?
    ) {
        prefs.edit().apply {
            putString(KEY_SESSION_TOKEN, sessionToken)
            putString(KEY_SESSION_TYPE, sessionType)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_PHONE, userPhone)
            putString(KEY_USER_EMAIL, userEmail)
            putString(KEY_EXPIRED_AT, expiredAt)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun clearSession() {
        val deviceId = prefs.getString(KEY_DEVICE_ID, null)
        val remember = rememberMe
        val phone = savedPhone
        val pin = savedPin
        prefs.edit().clear().apply()
        // Preserve device ID and saved credentials across logouts
        prefs.edit().apply {
            if (deviceId != null) putString(KEY_DEVICE_ID, deviceId)
            if (remember) {
                putBoolean(KEY_REMEMBER_ME, true)
                if (phone != null) putString(KEY_SAVED_PHONE, phone)
                if (pin != null) putString(KEY_SAVED_PIN, pin)
            }
            apply()
        }
    }
}
