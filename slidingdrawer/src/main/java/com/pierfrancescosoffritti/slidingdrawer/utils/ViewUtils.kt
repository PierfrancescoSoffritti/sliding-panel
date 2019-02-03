package com.pierfrancescosoffritti.slidingdrawer.utils

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.pierfrancescosoffritti.slidingdrawer.Side

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

    fun isMotionEventWithinBoundaries(touchEvent: MotionEvent, view: View): Boolean {
        val touchX = touchEvent.rawX
        val touchY = touchEvent.rawY

        val viewCoordinates = IntArray(2)
        view.getLocationInWindow(viewCoordinates)

        val viewX = viewCoordinates[0].toFloat()
        val viewY = viewCoordinates[1].toFloat()
        val viewWidth = view.width.toFloat()
        val viewHeight = view.height.toFloat()

        return !(touchX < viewX || touchX > viewX + viewWidth || touchY < viewY || touchY > viewY + viewHeight)
    }
}