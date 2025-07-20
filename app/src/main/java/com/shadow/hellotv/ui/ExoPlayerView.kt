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
            // Set User-Agent if provided
            channel.userAgent?.let { userAgent ->
                setUserAgent(userAgent)
            }

            // Set referer and additional headers
            val headersMap = mutableMapOf<String, String>()

            // Add referer if provided
            channel.referer?.let { referer ->
                headersMap["Referer"] = referer
            }

            // Parse and set additional headers from playerHeaders
            if (!channel.playerHeaders.isNullOrEmpty()) {
                try {
                    val headers = channel.playerHeaders.split("\n")
                        .associate { line ->
                            val parts = line.split(":", limit = 2)
                            if (parts.size == 2) {
                                parts[0].trim() to parts[1].trim()
                            } else {
                                "" to ""
                            }
                        }
                        .filterKeys { it.isNotEmpty() }

                    headersMap.putAll(headers)
                } catch (e: Exception) {
                    e.printStackTrace()
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

        // Determine MIME type based on URL
        val mimeType = when {
            channel.url.contains(".mpd") -> MimeTypes.APPLICATION_MPD
            channel.url.contains(".m3u8") -> MimeTypes.APPLICATION_M3U8
            else -> null
        }

        mimeType?.let { builder.setMimeType(it) }

        // Set DRM configuration if DRM URL is provided
        if (!channel.drmUrl.isNullOrEmpty()) {
            try {
                val drmBuilder = MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID)
                    .setLicenseUri(channel.drmUrl)

                // Add DRM headers if provided
                if (!channel.drmHeaders.isNullOrEmpty()) {
                    val drmHeadersMap = channel.drmHeaders.split("\n")
                        .associate { line ->
                            val parts = line.split(":", limit = 2)
                            if (parts.size == 2) {
                                parts[0].trim() to parts[1].trim()
                            } else {
                                "" to ""
                            }
                        }
                        .filterKeys { it.isNotEmpty() }

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