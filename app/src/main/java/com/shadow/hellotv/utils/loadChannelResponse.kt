//package com.shadow.hellotv.utils
//
//import com.shadow.hellotv.model.ChannelItem
//import com.shadow.hellotv.model.ChannelResponse
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.net.HttpURLConnection
//import java.net.URL
//
//suspend fun loadChannelResponse(url: String): ChannelResponse = withContext(Dispatchers.IO) {
//    try {
//        val connection = URL(url).openConnection() as HttpURLConnection
//        connection.requestMethod = "GET"
//        connection.connectTimeout = 10000
//        connection.readTimeout = 10000
//        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android TV)")
//
//        val content = connection.inputStream.bufferedReader().use { it.readText() }
//        connection.disconnect()
//
//        val channels = parseM3UToChannelItems(content)
//
//        ChannelResponse(
//            success = true,
//            message = "Channels loaded successfully",
//            data = channels
//        )
//    } catch (e: Exception) {
//        e.printStackTrace()
//        ChannelResponse(
//            success = false,
//            message = "Failed to load channels: ${e.message}",
//            data = emptyList()
//        )
//    }
//}
