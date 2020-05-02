package com.akbolatss.workshop.slidingpanel.utils

import kotlin.math.abs


internal object Utils {

    /**
     * Normalize a screen coordinate value to interval [0,1].
     * The panel is expanded (1) when the screen coordinate is at the top (left) of the screen.
     * The panel is collapsed (0) when the screen coordinate is at the bottom (right) of the screen.
     */
    fun normalizeScreenCoordinate(value: Float, max: Float): Float {
        return abs(value - max) / max
    }

    /**
     * Restrict value to given range
     */
    fun clamp(value: Float, min: Float, max: Float): Float {
        return when {
            value > max -> max
            value < min -> min
            else -> value
        }
    }
}
