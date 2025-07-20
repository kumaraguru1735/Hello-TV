package com.shadow.hellotv.utils

import com.shadow.hellotv.TVChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

suspend fun loadPlaylist(url: String): List<TVChannel> = withContext(Dispatchers.IO) {
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android TV)")

        val content = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()

        parseM3U(content)
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
