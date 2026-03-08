package com.shadow.hellotv.network

import com.shadow.hellotv.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "HelloTV"

// Use println for logging since MIUI blocks Log.e for custom tags
private fun log(msg: String) {
    Log.e(TAG, msg)
    println("[$TAG] $msg")
}
const val BASE_URL = "https://iptv.helloiptv.in/api"

// Manually store cookies from server responses (more reliable than CookieManager)
// Only the login endpoint sets the real session cookie - other endpoints may create new sessions
private val storedCookies = mutableMapOf<String, String>()
private var loginCookieLocked = false

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T, val httpCode: Int = 200) : ApiResult<T>()
    data class Error(
        val message: String,
        val errorCode: String? = null,
        val httpCode: Int = 0,
        val data: String? = null
    ) : ApiResult<Nothing>()
}

private suspend fun postRequest(endpoint: String, body: String): Pair<Int, String> =
    withContext(Dispatchers.IO) {
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            // Send stored cookies with every request
            if (storedCookies.isNotEmpty()) {
                val cookieHeader = storedCookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
                connection.setRequestProperty("Cookie", cookieHeader)
                log( "Sending cookies: $cookieHeader")
            }

            connection.outputStream.bufferedWriter().use { it.write(body) }

            val code = connection.responseCode

            // Capture Set-Cookie headers only if we haven't locked the login cookie yet
            if (!loginCookieLocked) {
                connection.headerFields["Set-Cookie"]?.forEach { cookie ->
                    val parts = cookie.split(";")[0].split("=", limit = 2)
                    if (parts.size == 2) {
                        val name = parts[0].trim()
                        val value = parts[1].trim()
                        if (value != "deleted" && value.isNotEmpty()) {
                            storedCookies[name] = value
                            log( "Stored cookie: $name=${value.take(20)}...")
                        }
                    }
                }
            }

            val response = try {
                connection.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            log( "API [$code] $endpoint -> ${response.take(500)}")
            Pair(code, response)
        } finally {
            connection.disconnect()
        }
    }

object ApiService {

    // ── Login ──
    suspend fun login(request: LoginRequest): ApiResult<LoginData> {
        return try {
            val body = json.encodeToString(request)
            log( "Login request: $body")
            val (code, response) = postRequest("$BASE_URL/auth.php?action=login", body)
            log( "Login response code: $code")
            val apiResponse = json.decodeFromString<ApiResponse<LoginData>>(response)
            log( "Login parsed: success=${apiResponse.success}, errorCode=${apiResponse.errorCode}, message=${apiResponse.message}")

            if (apiResponse.success && apiResponse.data != null) {
                // Lock the cookie so other endpoints can't replace it
                loginCookieLocked = true
                log( "Login success - cookies locked: $storedCookies")
                ApiResult.Success(apiResponse.data, code)
            } else {
                ApiResult.Error(
                    message = apiResponse.message,
                    errorCode = apiResponse.errorCode,
                    httpCode = code,
                    data = response
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log( "Login exception: ${e.message} ${e.stackTraceToString().take(200)}")
            ApiResult.Error(
                message = e.message ?: "Connection failed",
                httpCode = 0
            )
        }
    }

    // ── Validate Session ──
    suspend fun validateSession(sessionToken: String): ApiResult<ValidateData> {
        return try {
            val body = """{"session_token":"$sessionToken"}"""
            val (code, response) = postRequest("$BASE_URL/auth.php?action=validate", body)
            val apiResponse = json.decodeFromString<ApiResponse<ValidateData>>(response)

            if (apiResponse.success && apiResponse.data != null) {
                ApiResult.Success(apiResponse.data, code)
            } else {
                ApiResult.Error(
                    message = apiResponse.message,
                    errorCode = apiResponse.errorCode,
                    httpCode = code
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Connection failed")
        }
    }

    // ── Logout ──
    fun clearCookies() {
        storedCookies.clear()
        loginCookieLocked = false
        log( "Cookies cleared")
    }

    suspend fun logout(sessionToken: String): ApiResult<Unit> {
        return try {
            val body = """{"session_token":"$sessionToken"}"""
            val (code, response) = postRequest("$BASE_URL/auth.php?action=logout", body)
            val apiResponse = json.decodeFromString<ApiResponse<Unit?>>(response)

            if (apiResponse.success) {
                ApiResult.Success(Unit, code)
            } else {
                ApiResult.Error(message = apiResponse.message, errorCode = apiResponse.errorCode, httpCode = code)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Connection failed")
        }
    }

    // ── Get Sessions ──
    suspend fun getSessions(sessionToken: String): ApiResult<SessionsData> {
        return try {
            val body = """{"session_token":"$sessionToken"}"""
            val (code, response) = postRequest("$BASE_URL/auth.php?action=sessions", body)
            val apiResponse = json.decodeFromString<ApiResponse<SessionsData>>(response)

            if (apiResponse.success && apiResponse.data != null) {
                ApiResult.Success(apiResponse.data, code)
            } else {
                ApiResult.Error(message = apiResponse.message, errorCode = apiResponse.errorCode, httpCode = code)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Connection failed")
        }
    }

    // ── Kick Device ──
    suspend fun kickDevice(sessionToken: String, targetSessionId: Int): ApiResult<KickData> {
        return try {
            val body = """{"session_token":"$sessionToken","target_session_id":$targetSessionId}"""
            val (code, response) = postRequest("$BASE_URL/auth.php?action=kick_device", body)
            val apiResponse = json.decodeFromString<ApiResponse<KickData>>(response)

            if (apiResponse.success && apiResponse.data != null) {
                ApiResult.Success(apiResponse.data, code)
            } else {
                ApiResult.Error(message = apiResponse.message, errorCode = apiResponse.errorCode, httpCode = code)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Connection failed")
        }
    }

    // ── Replace Device ──
    suspend fun replaceDevice(sessionToken: String, targetSessionId: Int): ApiResult<ReplaceData> {
        return try {
            val body = """{"session_token":"$sessionToken","target_session_id":$targetSessionId}"""
            val (code, response) = postRequest("$BASE_URL/auth.php?action=replace_device", body)
            val apiResponse = json.decodeFromString<ApiResponse<ReplaceData>>(response)

            if (apiResponse.success && apiResponse.data != null) {
                ApiResult.Success(apiResponse.data, code)
            } else {
                ApiResult.Error(message = apiResponse.message, errorCode = apiResponse.errorCode, httpCode = code)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Connection failed")
        }
    }

    // ── Get Channels ──
    suspend fun getChannels(sessionToken: String): ApiResult<ChannelsData> {
        return try {
            val body = """{"session_token":"$sessionToken"}"""
            val (code, response) = postRequest("$BASE_URL/channels.php", body)
            val apiResponse = json.decodeFromString<ApiResponse<ChannelsData>>(response)

            if (apiResponse.success && apiResponse.data != null) {
                ApiResult.Success(apiResponse.data, code)
            } else {
                ApiResult.Error(message = apiResponse.message, errorCode = apiResponse.errorCode, httpCode = code)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Connection failed")
        }
    }

    // ── Get Categories ──
    suspend fun getCategories(sessionToken: String): ApiResult<CategoriesData> {
        return try {
            val body = """{"session_token":"$sessionToken"}"""
            val (code, response) = postRequest("$BASE_URL/categories.php", body)
            val apiResponse = json.decodeFromString<ApiResponse<CategoriesData>>(response)

            if (apiResponse.success && apiResponse.data != null) {
                ApiResult.Success(apiResponse.data, code)
            } else {
                ApiResult.Error(message = apiResponse.message, errorCode = apiResponse.errorCode, httpCode = code)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Connection failed")
        }
    }

    // ── Get Languages ──
    suspend fun getLanguages(sessionToken: String): ApiResult<LanguagesData> {
        return try {
            val body = """{"session_token":"$sessionToken"}"""
            val (code, response) = postRequest("$BASE_URL/languages.php", body)
            val apiResponse = json.decodeFromString<ApiResponse<LanguagesData>>(response)

            if (apiResponse.success && apiResponse.data != null) {
                ApiResult.Success(apiResponse.data, code)
            } else {
                ApiResult.Error(message = apiResponse.message, errorCode = apiResponse.errorCode, httpCode = code)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.Error(message = e.message ?: "Connection failed")
        }
    }
}
