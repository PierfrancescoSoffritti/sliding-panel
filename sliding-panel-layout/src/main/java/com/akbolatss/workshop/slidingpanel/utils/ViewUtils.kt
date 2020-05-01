package com.akbolatss.workshop.slidingpanel.utils

import android.view.View
import android.view.ViewGroup
import com.akbolatss.workshop.slidingpanel.Side

internal object ViewUtils {
    /**
     * Adds margin to the bottom of a view.
     * @param view the view that needs padding
     */
    fun setMargin(view: View, offset: Int, side: Side) {
        val layoutParams = view.layoutParams
        val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams

        val leftMargin = marginLayoutParams.leftMargin + (if(side == Side.LEFT) 1 else 0) * offset
        val topMargin = marginLayoutParams.topMargin + (if(side == Side.TOP) 1 else 0) * offset
        val rightMargin = marginLayoutParams.rightMargin + (if(side == Side.RIGHT) 1 else 0) * offset
        val bottomMargin = marginLayoutParams.bottomMargin + (if(side == Side.BOTTOM) 1 else 0) * offset

        layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
        view.layoutParams = layoutParams
    }

    /**
     * Adds padding to the bottom of a view.
     * @param view the view that needs padding
     */
    fun setPadding(view: View, offset: Int, side: Side) {
        val paddingLeft = view.paddingLeft + (if(side == Side.LEFT) 1 else 0) * offset
        val paddingTop = view.paddingTop + (if(side == Side.TOP) 1 else 0) * offset
        val paddingRight = view.paddingRight + (if(side == Side.RIGHT) 1 else 0) * offset
        val paddingBottom = view.paddingBottom + (if(side == Side.BOTTOM) 1 else 0) * offset

        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }
}
