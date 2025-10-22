package com.shadow.hellotv.ui

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.shadow.hellotv.model.ChannelItem
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@UnstableApi
@Composable
fun ExoPlayerView(
    channel: ChannelItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        // Create HTTP data source factory with custom headers
        val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
            val headersMap = mutableMapOf<String, String>()

            // Parse userAgent (JsonElement → String)
            channel.userAgent?.let { element ->
                when {
                    element is JsonPrimitive && element.isString -> {
                        setUserAgent(element.content)
                    }
                    element is JsonObject -> {
                        // You can also include User-Agent inside this object if you prefer
                        element["User-Agent"]?.jsonPrimitive?.contentOrNull?.let { ua ->
                            setUserAgent(ua)
                        }
                        // Add all other entries as headers
                        element.forEach { (key, value) ->
                            headersMap[key] = value.jsonPrimitive.content
                        }
                    }
                }
            }

            // Add referer if provided
            channel.referer?.let { referer ->
                headersMap["Referer"] = referer
            }

            // Parse playerHeaders (JsonElement → Map)
            channel.playerHeaders?.let { element ->
                when (element) {
                    is JsonPrimitive -> {
                        // handle if it's a string like "Header: Value\nAnother: Header"
                        val headers = element.content.split("\n")
                            .mapNotNull { line ->
                                val parts = line.split(":", limit = 2)
                                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                            }.toMap()
                        headersMap.putAll(headers)
                    }

                    is JsonObject -> {
                        // handle JSON object headers
                        element.forEach { (key, value) ->
                            headersMap[key] = value.jsonPrimitive.content
                        }
                    }

                    else -> Unit
                }
            }

            if (headersMap.isNotEmpty()) {
                setDefaultRequestProperties(headersMap)
            }
        }

        val mediaSourceFactory = if (channel.url.startsWith("rtmp://", ignoreCase = true)) {
            val rtmpFactory = androidx.media3.datasource.rtmp.RtmpDataSource.Factory()
            DefaultMediaSourceFactory(rtmpFactory)
        } else {
            val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
            DefaultMediaSourceFactory(dataSourceFactory)
        }

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    val mediaItem = remember(channel) {
        val builder = MediaItem.Builder()
            .setUri(channel.url)

        val mimeType = when {
            channel.url.contains(".mpd") -> MimeTypes.APPLICATION_MPD
            channel.url.contains(".m3u8") -> MimeTypes.APPLICATION_M3U8
            else -> null
        }

        mimeType?.let { builder.setMimeType(it) }

        if (!channel.drmUrl.isNullOrEmpty()) {
            try {
                val drmBuilder = MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID)
                    .setLicenseUri(channel.drmUrl)

                // DRM Headers (JsonElement → Map)
                channel.drmHeaders?.let { element ->
                    val drmHeadersMap = when (element) {
                        is JsonPrimitive -> element.content.split("\n").mapNotNull { line ->
                            val parts = line.split(":", limit = 2)
                            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                        }.toMap()

                        is JsonObject -> element.mapValues { it.value.jsonPrimitive.content }

                        else -> emptyMap()
                    }

                    if (drmHeadersMap.isNotEmpty()) {
                        drmBuilder.setLicenseRequestHeaders(drmHeadersMap)
                    }
                }

                builder.setDrmConfiguration(drmBuilder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        builder.build()
    }

    LaunchedEffect(mediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
