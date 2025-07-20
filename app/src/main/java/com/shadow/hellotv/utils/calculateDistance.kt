package com.shadow.hellotv.utils

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt

fun calculateDistance(start: Offset, end: Offset): Float {
    val deltaX = end.x - start.x
    val deltaY = end.y - start.y
    return sqrt(deltaX * deltaX + deltaY * deltaY)
}