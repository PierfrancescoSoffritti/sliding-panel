package com.pierfrancescosoffritti.slidingdrawer

enum class PanelState {
    EXPANDED, COLLAPSED, SLIDING
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