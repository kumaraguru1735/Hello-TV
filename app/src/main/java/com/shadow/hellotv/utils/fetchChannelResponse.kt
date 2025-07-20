package com.shadow.hellotv.utils

import com.shadow.hellotv.model.ChannelItem
import com.shadow.hellotv.model.ChannelResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

val jsonParser = Json {
    ignoreUnknownKeys = true
}

suspend fun fetchChannelResponse(apiUrl: String): ChannelResponse = withContext(Dispatchers.IO) {
    val connection = URL(apiUrl).openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("Accept", "application/json")

    try {
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
            throw Exception("HTTP Error: ${connection.responseCode} ${connection.responseMessage}, Details: $errorText")
        }
        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
        println("Raw API Response: $responseText")
        if (responseText.isEmpty()) {
            throw Exception("Empty response from API")
        }
        jsonParser.decodeFromString<ChannelResponse>(responseText)
    } catch (e: Exception) {
        throw Exception("Failed to parse JSON: ${e.message}")
    } finally {
        connection.disconnect()
    }
}

suspend fun loadPlaylist(url: String): List<ChannelItem> {
    return try {
        val response = fetchChannelResponse(url)
        if (response.success) {
            response.data
        } else {
            throw Exception("API Error: ${response.message}")
        }
    } catch (e: Exception) {
        throw Exception("Failed to load playlist: ${e.message}")
    }
}