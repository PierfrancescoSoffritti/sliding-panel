package com.akbolatss.workshop.slidingpanel

enum class PanelState(val value: Float) {
    COLLAPSED(1f), EXPANDED(0f), SLIDING(-1f)
}

internal enum class SlidingDirection {
    UP_OR_LEFT, DOWN_OR_RIGHT, NONE
}

internal enum class Side {
    TOP, BOTTOM, LEFT, RIGHT
}

internal enum class Orientation {
    VERTICAL, HORIZONTAL
}