package com.pierfrancescosoffritti.slidingdrawer

import android.view.View
import android.view.ViewGroup


object Utils {
    /**
     * Normalize value to interval [0,1]
     */
    fun normalize(value: Float, max: Float): Float {
        return Math.abs(value - max) / max
    }

    /**
     * Adds margin to the bottom of a view.
     * @param view the view that needs padding
     */
    fun setBottomMargin(view: View, offset: Int) {
        val layoutParams = view.layoutParams
        (layoutParams as ViewGroup.MarginLayoutParams).setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin+offset)
        view.layoutParams = layoutParams
    }

    /**
     * Adds padding to the bottom of a view.
     * @param view the view that needs padding
     */
    fun setBottomPadding(view: View, offset: Int) {
        view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom+offset)
    }
}