package com.akbolatss.workshop.slidingpanel.utils

import android.view.MotionEvent
import android.view.View
import com.akbolatss.workshop.slidingpanel.Orientation
import com.akbolatss.workshop.slidingpanel.SlidingPanelLayout

internal object Extensions {

    fun SlidingPanelLayout.isOrientationVertical(): Boolean {
        return orientation == Orientation.VERTICAL
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
