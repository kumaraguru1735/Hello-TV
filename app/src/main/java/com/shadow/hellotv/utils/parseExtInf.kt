package com.shadow.hellotv.utils

fun parseExtInf(line: String): Map<String, String> {
    val result = mutableMapOf<String, String>()
    try {
        // Match attributes like tvg-id="abc", group-title="xyz"
        val attributePattern = Regex("""(\w+(?:-\w+)*)=(?:"([^"]*)"|([^\s,]+))""")
        val matches = attributePattern.findAll(line)

        for (match in matches) {
            val key = match.groupValues[1]
            val value = match.groupValues[2].ifEmpty { match.groupValues[3] }
            result[key] = value
        }

        // Get channel display name (after last comma)
        val nameStart = line.lastIndexOf(',')
        if (nameStart != -1 && nameStart < line.length - 1) {
            result["name"] = line.substring(nameStart + 1).trim()
        }
    } catch (e: Exception) {
        // In case of parse error, fallback to just name extraction
        val nameStart = line.lastIndexOf(',')
        if (nameStart != -1 && nameStart < line.length - 1) {
            result["name"] = line.substring(nameStart + 1).trim()
        }
    }
    return result
}