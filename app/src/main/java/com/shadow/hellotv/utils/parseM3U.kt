package com.shadow.hellotv.utils

import com.shadow.hellotv.TVChannel

fun parseM3U(content: String): List<TVChannel> {
    val channels = mutableListOf<TVChannel>()
    val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }

    var i = 0
    while (i < lines.size) {
        val line = lines[i]

        if (line.startsWith("#EXTINF:")) {
            val channelInfo = parseExtInf(line)
            var licenseKey: String? = null

            var j = i + 1
            // Parse optional KODIPROP license key lines
            while (j < lines.size && lines[j].startsWith("#KODIPROP:")) {
                val prop = lines[j]
                if (prop.contains("license_key=")) {
                    licenseKey = prop.substringAfter("license_key=")
                }
                j++
            }

            // Skip empty/comment lines until we find the stream URL
            while (j < lines.size && (lines[j].startsWith("#") || lines[j].isBlank())) {
                j++
            }

            if (j < lines.size) {
                val streamUrl = lines[j]

                channels.add(
                    TVChannel(
                        id = channelInfo["tvg-id"] ?: "",
                        name = channelInfo["name"] ?: "Unknown Channel",
                        logo = channelInfo["tvg-logo"] ?: "",
                        group = channelInfo["group-title"] ?: "",
                        url = streamUrl,
                        licenseKey = licenseKey
                    )
                )
                i = j + 1
            } else {
                i++
            }
        } else {
            i++
        }
    }

    return channels.filter { it.url.isNotEmpty() }
}