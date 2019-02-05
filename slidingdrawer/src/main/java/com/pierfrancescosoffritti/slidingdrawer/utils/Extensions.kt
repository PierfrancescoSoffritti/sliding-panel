package com.pierfrancescosoffritti.slidingdrawer.utils

import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.pierfrancescosoffritti.slidingdrawer.SlidingPanel

internal object Extensions {
    fun SlidingPanel.isOrientationVertical(): Boolean {
        return orientation == LinearLayout.VERTICAL
    }

    fun View.isMotionEventWithinBounds(motionEvent: MotionEvent): Boolean {
        val touchX = motionEvent.rawX
        val touchY = motionEvent.rawY

        val viewCoordinates = IntArray(2)
        getLocationInWindow(viewCoordinates)

        val viewX = viewCoordinates[0].toFloat()
        val viewY = viewCoordinates[1].toFloat()
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        return !(touchX < viewX || touchX > viewX + viewWidth || touchY < viewY || touchY > viewY + viewHeight)
    }
}