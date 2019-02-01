package com.pierfrancescosoffritti.slidingdrawer

import android.view.View

object Utils {
    /**
     * Normalize value to interval [0,1]
     */
    fun normalize(value: Float, max: Float): Float {
        return Math.abs(value - max) / max
    }

    /**
     * Adds padding to the bottom of a view.
     * @param root the view that needs padding
     */
    fun setPaddingBottom(root: View, offset: Int) {
        val topPadding = root.paddingTop
        val leftPadding = root.paddingLeft
        val rightPadding = root.paddingRight
        val bottomPadding = root.paddingRight

        // why not setPaddingRelative?
        root.setPadding(leftPadding, topPadding, rightPadding, bottomPadding+offset)
    }
}