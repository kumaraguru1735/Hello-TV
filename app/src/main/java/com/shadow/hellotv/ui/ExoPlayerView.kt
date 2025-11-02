package com.shadow.hellotv.ui

import android.util.Log
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
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.shadow.hellotv.model.ChannelItem
import com.shadow.hellotv.model.DRMType
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private const val TAG = "ExoPlayerView"

@UnstableApi
@Composable
fun ExoPlayerView(
    channel: ChannelItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Parse headers and create factory for each channel
    val (mediaSourceFactory, mediaItem) = remember(channel.id, channel.url) {
        // Parse headers
        var userAgent = "Mozilla/5.0 (Linux; Android 10)"
        val headersMap = mutableMapOf<String, String>()

        channel.playerHeaders?.let { element ->
            when (element) {
                is JsonPrimitive -> {
                    element.content.split("\n").forEach { line ->
                        val parts = line.split(":", limit = 2)
                        if (parts.size == 2) {
                            val key = parts[0].trim()
                            val value = parts[1].trim()
                            if (key.equals("User-Agent", ignoreCase = true)) {
                                userAgent = value
                            } else {
                                headersMap[key] = value
                            }
                        }
                    }
                }
                is JsonObject -> {
                    element.forEach { (key, value) ->
                        val headerValue = value.jsonPrimitive.contentOrNull ?: value.jsonPrimitive.content
                        if (key.equals("User-Agent", ignoreCase = true)) {
                            userAgent = headerValue
                        } else {
                            headersMap[key] = headerValue
                        }
                    }
                }
                else -> {}
            }
        }

        Log.d(TAG, "📋 Headers for ${channel.name}: $headersMap")

        // Create HTTP data source factory with headers
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
            .setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS)
            .apply {
                if (headersMap.isNotEmpty()) {
                    setDefaultRequestProperties(headersMap)
                }
            }

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        val sourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        // Build media item
        val builder = MediaItem.Builder().setUri(channel.url)

        // Set MIME type
        val mimeType = when {
            channel.url.contains(".mpd", ignoreCase = true) -> MimeTypes.APPLICATION_MPD
            channel.url.contains(".m3u8", ignoreCase = true) -> MimeTypes.APPLICATION_M3U8
            channel.url.contains(".mp4", ignoreCase = true) -> MimeTypes.APPLICATION_MP4
            else -> null
        }
        mimeType?.let { builder.setMimeType(it) }

        // Configure DRM
        if (!channel.drmUrl.isNullOrEmpty() && channel.drmType != null) {
            val drmUuid = when (channel.drmType) {
                DRMType.CLEARKEY -> C.CLEARKEY_UUID
                DRMType.WIDEVINE -> C.WIDEVINE_UUID
                DRMType.PLAYREADY -> C.PLAYREADY_UUID
                DRMType.FAIRPLAY -> null
            }

            drmUuid?.let { uuid ->
                Log.d(TAG, "🔐 DRM: ${channel.drmType} for ${channel.name}")
                val drmBuilder = MediaItem.DrmConfiguration.Builder(uuid)
                    .setLicenseUri(channel.drmUrl)

                channel.drmHeaders?.let { element ->
                    val drmHeadersMap = when (element) {
                        is JsonPrimitive -> {
                            element.content.split("\n").mapNotNull { line ->
                                val parts = line.split(":", limit = 2)
                                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                            }.toMap()
                        }
                        is JsonObject -> {
                            element.mapValues { it.value.jsonPrimitive.content }
                        }
                        else -> emptyMap()
                    }

                    if (drmHeadersMap.isNotEmpty()) {
                        drmBuilder.setLicenseRequestHeaders(drmHeadersMap)
                    }
                }

                builder.setDrmConfiguration(drmBuilder.build())
            }
        }

        Pair(sourceFactory, builder.build())
    }

    // Create new ExoPlayer for each channel
    val exoPlayer = remember(channel.id) {
        Log.d(TAG, "🎬 Creating NEW ExoPlayer for: ${channel.name}")

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val state = when (playbackState) {
                            Player.STATE_IDLE -> "IDLE"
                            Player.STATE_BUFFERING -> "BUFFERING"
                            Player.STATE_READY -> "READY"
                            Player.STATE_ENDED -> "ENDED"
                            else -> "UNKNOWN"
                        }
                        Log.d(TAG, "🎮 ${channel.name} - State: $state")
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        Log.e(TAG, "❌ ${channel.name} - Error: ${error.errorCodeName}", error)
                    }
                })

                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            }
    }

    // Load and play media
    LaunchedEffect(channel.id) {
        Log.d(TAG, "▶️ Loading: ${channel.name}")
        Log.d(TAG, "🔗 URL: ${channel.url}")

        try {
            // Small delay to ensure previous player is fully released
            delay(50)

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            Log.d(TAG, "✅ Started playing: ${channel.name}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to play ${channel.name}", e)
        }
    }

    // Cleanup when channel changes
    DisposableEffect(channel.id) {
        onDispose {
            Log.d(TAG, "🛑 Releasing player for: ${channel.name}")
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            Log.d(TAG, "🖼️ Creating PlayerView for: ${channel.name}")
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { view ->
            // Critical: Update the player reference when it changes
            Log.d(TAG, "🔄 Updating PlayerView with new player")
            view.player = exoPlayer
        },
        modifier = modifier.fillMaxSize()
    )
}