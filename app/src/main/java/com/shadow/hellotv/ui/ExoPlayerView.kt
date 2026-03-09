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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.shadow.hellotv.model.Channel
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private const val TAG = "ExoPlayerView"
private fun log(msg: String) { Log.e(TAG, msg); println("[$TAG] $msg") }

@UnstableApi
@Composable
fun ExoPlayerView(
    channel: Channel,
    onPlayerReady: (ExoPlayer) -> Unit = {},
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Parse headers and create factory for each channel
    val (mediaSourceFactory, mediaItem) = remember(channel.id, channel.url) {
        var userAgent = "Mozilla/5.0 (Linux; Android 10)"
        val headersMap = mutableMapOf<String, String>()

        // Parse player headers
        channel.headers?.let { element ->
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

        log( "Headers for ${channel.name}: $headersMap")

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

        // Set MIME type - prioritize URL extension over streamType (API streamType can be wrong)
        val mimeType = when {
            channel.url.contains(".mpd", ignoreCase = true) -> MimeTypes.APPLICATION_MPD
            channel.url.contains(".m3u8", ignoreCase = true) -> MimeTypes.APPLICATION_M3U8
            channel.url.contains(".mp4", ignoreCase = true) -> MimeTypes.APPLICATION_MP4
            channel.streamType.equals("dash", ignoreCase = true) -> MimeTypes.APPLICATION_MPD
            channel.streamType.equals("hls", ignoreCase = true) -> MimeTypes.APPLICATION_M3U8
            else -> null // Let ExoPlayer auto-detect
        }
        log( "Channel: ${channel.name} | URL: ${channel.url.takeLast(40)} | streamType: ${channel.streamType} | mimeType: $mimeType")
        mimeType?.let { builder.setMimeType(it) }

        // Configure DRM
        if (!channel.drmLicenceUrl.isNullOrEmpty() && !channel.drmType.isNullOrEmpty()) {
            val drmUuid = when (channel.drmType.lowercase()) {
                "clearkey" -> C.CLEARKEY_UUID
                "widevine" -> C.WIDEVINE_UUID
                "playready" -> C.PLAYREADY_UUID
                else -> null
            }

            drmUuid?.let { uuid ->
                log( "DRM: ${channel.drmType} for ${channel.name}")
                val drmBuilder = MediaItem.DrmConfiguration.Builder(uuid)
                    .setLicenseUri(channel.drmLicenceUrl)

                channel.drmLicenceHeaders?.let { element ->
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

    // Create ExoPlayer with HEVC software decoding fallback
    val exoPlayer = remember(channel.id) {
        log( "Creating ExoPlayer for: ${channel.name}")

        // Enable software decoder fallback for HEVC/H.265 content
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            .setEnableDecoderFallback(true)

        ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
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
                        log( "${channel.name} - State: $state")
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        log( "${channel.name} - Error: ${error.errorCodeName} ${error.message}")
                    }
                })

                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            }
    }

    // Expose player to parent
    LaunchedEffect(exoPlayer) {
        onPlayerReady(exoPlayer)
    }

    // Load and play media
    LaunchedEffect(channel.id) {
        log( "Loading: ${channel.name} | URL: ${channel.url}")

        try {
            delay(50)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            log( "Started playing: ${channel.name}")
        } catch (e: Exception) {
            log( "Failed to play ${channel.name}: ${e.message}")
        }
    }

    // Pause on background, resume on foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    log("Pausing player (app backgrounded)")
                    exoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    log("Resuming player (app foregrounded)")
                    exoPlayer.playWhenReady = true
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Cleanup when channel changes
    DisposableEffect(channel.id) {
        onDispose {
            log( "Releasing player for: ${channel.name}")
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                this.resizeMode = resizeMode
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { view ->
            view.player = exoPlayer
            view.resizeMode = resizeMode
        },
        modifier = modifier.fillMaxSize()
    )
}
