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
    fun setMargin(view: View, offset: Int, side: Int) {
        val layoutParams = view.layoutParams
        val marginLayoutParams = (layoutParams as ViewGroup.MarginLayoutParams)

        val leftMargin = marginLayoutParams.leftMargin + (if(side == 0) 1 else 0) * offset
        val topMargin = marginLayoutParams.topMargin + (if(side == 1) 1 else 0) * offset
        val rightMargin = marginLayoutParams.rightMargin + (if(side == 2) 1 else 0) * offset
        val bottomMargin = marginLayoutParams.bottomMargin + (if(side == 3) 1 else 0) * offset

        layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
        view.layoutParams = layoutParams
    }

    /**
     * Adds padding to the bottom of a view.
     * @param view the view that needs padding
     */
    fun setPadding(view: View, offset: Int, side: Int) {
        val paddingLeft = view.paddingLeft + (if(side == 0) 1 else 0) * offset
        val paddingTop = view.paddingTop + (if(side == 1) 1 else 0) * offset
        val paddingRight = view.paddingRight + (if(side == 2) 1 else 0) * offset
        val paddingBottom = view.paddingBottom + (if(side == 3) 1 else 0) * offset

        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }
}