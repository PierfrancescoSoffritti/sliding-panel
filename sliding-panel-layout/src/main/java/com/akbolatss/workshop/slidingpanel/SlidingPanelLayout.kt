package com.akbolatss.workshop.slidingpanel

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import com.akbolatss.workshop.slidingpanel.utils.Extensions.isMotionEventWithinBounds
import com.akbolatss.workshop.slidingpanel.utils.Extensions.isOrientationVertical
import com.akbolatss.workshop.slidingpanel.utils.Utils
import com.akbolatss.workshop.slidingpanel.utils.ViewUtils
import java.lang.Float.isNaN
import java.util.*
import kotlin.math.abs

/**
 * Custom View implementing a sliding panel (bottom sheet pattern) that is part of the view hierarchy, not above it.
 *
 * Read [detailed documentation here](https://github.com/iRYO400/sliding-panel)
 */
class SlidingPanelLayout(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    companion object {
        const val SLIDE_DURATION_SHORT = 300L
        const val SLIDE_DURATION_LONG = 600L

        // the color of the shade that fades over the nonSlidingView view when the slidingView slides over it
        private const val SHADE_COLOR_WITH_ALPHA = -0x67000000
        private const val SHADE_COLOR_MAX_ALPHA = SHADE_COLOR_WITH_ALPHA.ushr(24)
        private const val SHADE_COLOR = SHADE_COLOR_WITH_ALPHA and 0x00FFFFFF
    }

    private val touchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop

    private lateinit var slidingView: View
    private lateinit var nonSlidingView: View

    // the view sensible to swipe gestures. Drag this view to move the slidingView
    private lateinit var dragView: View

    // an optional view, child of slidingView, to which a margin bottom is added, in order to make it always fit on the screen.
    private var fittingView: View? = null

    internal val orientation: Orientation

    var state = PanelState.COLLAPSED
        private set

    private var previousState: PanelState? = null

    // A value between 1.0 and 0.0 (0.0 = COLLAPSED, 1.0 = EXPANDED)
    private var currentSlide = 0.0f

    // the maximum amount the slidingView can slide. It corresponds to the height (width) of the nonSlidingView.
    private var maxSlide = 0f

    // the minimum coordinate the slidingView can slide to. It corresponds to the top (right) of the nonSlidingView.
    private var minSlide = 0f

    // position of the slidingView recorded when the user touches the screen for the first time (eg. at the beginning of a swipe gesture)
    private var slidingViewPosAtFirstTouch = 0f

    // touch coordinates of the first touch gesture (eg. at the beginning of a swipe gesture)
    private var coordOfFirstTouch = 0f

    // Helper for computing gesture direction
    private var velocityTracker: VelocityTracker? = null

    // Last saved direction to complete slide
    private var lastDirection: SlidingDirection? = null

    private var isTouchGoneOutside = false

    private var isSliding = false

    private val elevationShadow90Deg =
        ContextCompat.getDrawable(getContext(), R.drawable.sp_elevation_shadow_90_deg)!!
    private val elevationShadow180Deg =
        ContextCompat.getDrawable(getContext(), R.drawable.sp_elevation_shadow_180_deg)!!

    @IdRes
    private val slidingViewId: Int

    @IdRes
    private val nonSlidingViewId: Int

    @IdRes
    private val fittingViewId: Int

    @IdRes
    private var dragViewId: Int

    private var fitScreenApplied = false

    private val listeners = HashSet<OnSlideListener>()

    /**
     * The duration of the slide in millisecond, when executed with an animation instead of a gesture
     */
    private var slideDuration = SLIDE_DURATION_SHORT


    private var slidingPanelShadowLengthPixels: Int = 0

    init {
        setWillNotDraw(false)

        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SlidingPanelLayout, 0, 0)

        try {
            slidingViewId = typedArray.getResourceId(R.styleable.SlidingPanelLayout_slidingView, -1)
            nonSlidingViewId =
                typedArray.getResourceId(R.styleable.SlidingPanelLayout_nonSlidingView, -1)
            dragViewId = typedArray.getResourceId(R.styleable.SlidingPanelLayout_dragView, -1)
            fittingViewId =
                typedArray.getResourceId(R.styleable.SlidingPanelLayout_fitToScreenView, -1)

            slidingPanelShadowLengthPixels = typedArray.getDimensionPixelSize(
                R.styleable.SlidingPanelLayout_elevation,
                resources.getDimensionPixelSize(R.dimen.sp_4dp)
            )

            val orientationEnumPos =
                typedArray.getInt(R.styleable.SlidingPanelLayout_orientation, 0)
            orientation = Orientation.values()[orientationEnumPos]

            val initialStateId = typedArray.getInt(R.styleable.SlidingPanelLayout_slidingState, 0)
            state = PanelState.values()[initialStateId]
        } finally {
            typedArray.recycle()
        }

        if (slidingViewId == -1)
            throw RuntimeException("SlidingPanel, app:slidingView attribute not set. You must set this attribute to the id of the view that you want to be sliding.")
        if (nonSlidingViewId == -1)
            throw RuntimeException("SlidingPanel, app:nonSlidingView attribute not set. You must set this attribute to the id of the view that you want to be static.")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (childCount != 2)
            throw IllegalStateException("SlidingPanel must have exactly 2 children. But has $childCount")

        slidingView = findViewById(slidingViewId)
            ?: throw RuntimeException("SlidingPanel, slidingView is null.")
        nonSlidingView = findViewById(nonSlidingViewId)
            ?: throw RuntimeException("SlidingPanel, nonSlidingView is null.")

        dragView = findViewById(dragViewId)
            ?: if (dragViewId != -1) throw RuntimeException("SlidingPanel, can't find dragView.")
            else slidingView

        fittingView = findViewById(fittingViewId)
            ?: if (fittingViewId != -1) throw RuntimeException("SlidingPanel, can't find fittingView.")
            else null
    }

    override fun onInterceptTouchEvent(currentTouchEvent: MotionEvent): Boolean {
        if (!dragView.isMotionEventWithinBounds(currentTouchEvent)) {
            isSliding = false
            return false
        }

        when (currentTouchEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                coordOfFirstTouch =
                    if (isOrientationVertical()) currentTouchEvent.y else currentTouchEvent.x
                slidingViewPosAtFirstTouch =
                    if (isOrientationVertical()) slidingView.y else slidingView.x
                isTouchGoneOutside = false
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val currentTouch: Float =
                    if (isOrientationVertical()) currentTouchEvent.y else currentTouchEvent.x
                val diff: Float = abs(currentTouch - coordOfFirstTouch)
                isSliding = diff > touchSlop / 4
                return isSliding
            }
            else -> {
                isSliding = false
                return false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "Recycle")
    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        if (isTouchGoneOutside)
            return false
        val currentTouchEvent = if (isOrientationVertical()) touchEvent.y else touchEvent.x

        when (touchEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                velocityTracker?.clear()
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                velocityTracker?.addMovement(touchEvent)
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.run {
                    val pointerId = touchEvent.getPointerId(touchEvent.actionIndex)
                    addMovement(touchEvent)
                    computeCurrentVelocity(1000)

                    val xVelocity = getXVelocity(pointerId)
                    val yVelocity = getYVelocity(pointerId)

                    lastDirection = computeDirection(xVelocity, yVelocity)
                }
                val isTouchGoneOutside = !dragView.isMotionEventWithinBounds(touchEvent)
                if (!isSliding && isTouchGoneOutside) {
                    if (isTouchGoneOutside) {
                        val preventiveTouch = MotionEvent.obtain(touchEvent).apply {
                            action = MotionEvent.ACTION_OUTSIDE
                        }
                        return dispatchTouchEvent(preventiveTouch)
                    }
                    return false
                }

                val touchOffset: Float = coordOfFirstTouch - currentTouchEvent
                var finalPosition: Float = slidingViewPosAtFirstTouch - touchOffset

                finalPosition = Utils.clamp(finalPosition, minSlide, maxSlide)
                updateState(Utils.normalizeScreenCoordinate(finalPosition, maxSlide))
            }
            MotionEvent.ACTION_UP -> {
                if (state == PanelState.SLIDING)
                    completeSlide(currentSlide, lastDirection)
                resetVelocityTracker()
            }
            MotionEvent.ACTION_CANCEL -> {
                completeSlide(currentSlide, lastDirection)
            }
            MotionEvent.ACTION_OUTSIDE -> {
                isTouchGoneOutside = true
                completeSlide(currentSlide, lastDirection)
            }
        }
        return true
    }

    private fun computeDirection(xVelocity: Float, yVelocity: Float): SlidingDirection {
        return if (abs(xVelocity) > abs(yVelocity))
            if (xVelocity > 0)
                SlidingDirection.DOWN_OR_RIGHT // right
            else
                SlidingDirection.UP_OR_LEFT // left
        else
            if (yVelocity > 0)
                SlidingDirection.DOWN_OR_RIGHT // down
            else
                SlidingDirection.UP_OR_LEFT // up
    }

    private fun resetVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun completeSlide(currentSlide: Float, direction: SlidingDirection?) {
        val targetSlide: Float = when (direction) {
            SlidingDirection.UP_OR_LEFT -> if (currentSlide > 0.1)
                minSlide
            else
                maxSlide
            SlidingDirection.DOWN_OR_RIGHT -> if (currentSlide < 0.9)
                maxSlide
            else
                minSlide
            else -> return
        }

        slideTo(Utils.normalizeScreenCoordinate(targetSlide, maxSlide))
    }

    override fun performClick(): Boolean {
        super.performClick()
        toggle()
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var slidingPanelHeight = 0
        var slidingPanelWidth = 0
        var childrenCombinedMeasuredStates = 0

        for (i: Int in 0 until childCount) {
            val child: View = getChildAt(i)
            if (child.visibility == View.GONE)
                continue

            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)

            val layoutParams = child.layoutParams as FrameLayout.LayoutParams

            slidingPanelWidth =
                slidingPanelWidth.coerceAtLeast(child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin)
            slidingPanelHeight =
                slidingPanelHeight.coerceAtLeast(child.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin)

            childrenCombinedMeasuredStates =
                View.combineMeasuredStates(childrenCombinedMeasuredStates, child.measuredState)
        }

        slidingPanelHeight = slidingPanelHeight.coerceAtLeast(suggestedMinimumHeight)
        slidingPanelWidth = slidingPanelWidth.coerceAtLeast(suggestedMinimumWidth)

        setMeasuredDimension(
            View.resolveSizeAndState(
                slidingPanelWidth,
                widthMeasureSpec,
                childrenCombinedMeasuredStates
            ),
            View.resolveSizeAndState(
                slidingPanelHeight,
                heightMeasureSpec,
                childrenCombinedMeasuredStates
            )
        )
    }

    private val onLayoutContainerRect = Rect()
    private val onLayoutChildRect = Rect()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var currentTop = paddingTop
        var currentLeft = paddingLeft

        for (i: Int in 0 until childCount) {
            val child: View = getChildAt(i)

            if (child.visibility == View.GONE)
                continue

            val childLayoutParams = child.layoutParams as FrameLayout.LayoutParams

            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            onLayoutContainerRect.left = currentLeft + childLayoutParams.leftMargin
            onLayoutContainerRect.right = currentLeft + childWidth - childLayoutParams.rightMargin

            onLayoutContainerRect.top = currentTop + childLayoutParams.topMargin
            onLayoutContainerRect.bottom = currentTop + childHeight - childLayoutParams.bottomMargin

            if (isOrientationVertical())
                currentTop = onLayoutContainerRect.bottom
            else
                currentLeft = onLayoutContainerRect.right

            Gravity.apply(
                childLayoutParams.gravity,
                childWidth,
                childHeight,
                onLayoutContainerRect,
                onLayoutChildRect
            )

            child.layout(
                onLayoutChildRect.left,
                onLayoutChildRect.top,
                onLayoutChildRect.right,
                onLayoutChildRect.bottom
            )
        }

        if (state == PanelState.EXPANDED) {
            updateState(1f)
        } else if (state == PanelState.COLLAPSED) {
            updateState(0f)
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (isOrientationVertical())
            drawElevationShadow90(canvas)
        else
            drawElevationShadow180(canvas)
    }

    private fun drawElevationShadow90(canvas: Canvas) {
        val top = (slidingView.y - slidingPanelShadowLengthPixels).toInt()
        val bottom = slidingView.y.toInt()
        val left = slidingView.left
        val right = slidingView.right

        elevationShadow90Deg.setBounds(left, top, right, bottom)
        elevationShadow90Deg.draw(canvas)
    }

    private fun drawElevationShadow180(canvas: Canvas) {
        val top = slidingView.top
        val bottom = slidingView.bottom
        val left = (slidingView.x - slidingPanelShadowLengthPixels).toInt()
        val right = slidingView.x.toInt()

        elevationShadow180Deg.setBounds(left, top, right, bottom)
        elevationShadow180Deg.draw(canvas)
    }

    private val drawChildChildTempRect = Rect()
    private val shadePaint = Paint()

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val save = canvas.save()
        val result: Boolean

        when (child) {
            nonSlidingView -> {
                maxSlide = if (isOrientationVertical())
                    nonSlidingView.height.toFloat()
                else
                    nonSlidingView.width.toFloat()

                minSlide = if (isOrientationVertical())
                    nonSlidingView.top.toFloat()
                else
                    nonSlidingView.left.toFloat()

                canvas.getClipBounds(drawChildChildTempRect)
                result = super.drawChild(canvas, child, drawingTime)

                if (currentSlide > 0) {
                    val currentShadeAlpha = (SHADE_COLOR_MAX_ALPHA * currentSlide).toInt()
                    val currentShadeColor = currentShadeAlpha shl 24 or SHADE_COLOR
                    shadePaint.color = currentShadeColor
                    canvas.drawRect(drawChildChildTempRect, shadePaint)
                }
            }
            slidingView -> {
                applyFitToScreenOnce()
                result = super.drawChild(canvas, child, drawingTime)
            }
            else -> {
                result = super.drawChild(canvas, child, drawingTime)
            }
        }

        canvas.restoreToCount(save)
        return result
    }

    private fun applyFitToScreenOnce() {
        if (fitScreenApplied) return

        val side = if (isOrientationVertical()) Side.BOTTOM else Side.RIGHT
        if (fittingView != null)
            ViewUtils.setMargin(fittingView!!, maxSlide.toInt(), side)

        fitScreenApplied = true
    }

    /**
     * Use this method to automatically slide at a specific position
     * @param positionNormalized Normalized value (between 0.0 and 1.0) representing the final slide position
     */
    private fun slideTo(positionNormalized: Float) {
        if (isNaN(positionNormalized))
            throw IllegalArgumentException("Bad value. Can't slide to NaN")
        if (positionNormalized < 0 || positionNormalized > 1)
            throw IllegalArgumentException("Bad value. Can't slide to $positionNormalized. Value must be between 0 and 1")

        val va = ValueAnimator.ofFloat(currentSlide, positionNormalized)
        va.interpolator = DecelerateInterpolator(1.5f)
        va.duration = slideDuration
        va.addUpdateListener { animation -> updateState(animation.animatedValue as Float) }
        va.start()
    }

    /**
     * always use this method to update the position of the sliding view.
     * @param newSlideRatio new slide value, normalized between 0 and 1
     */
    private fun updateState(newSlideRatio: Float) {
        if (newSlideRatio < 0 || newSlideRatio > 1)
            throw IllegalArgumentException("Slide value \"$newSlideRatio\" should be normalized, between 0 and 1.")

        currentSlide = newSlideRatio
        state = when (currentSlide) {
            1f -> PanelState.EXPANDED
            0f -> PanelState.COLLAPSED
            else -> PanelState.SLIDING
        }

        val currentSlideNonNormalized = abs(currentSlide * maxSlide - maxSlide)
        if (isOrientationVertical())
            slidingView.y = currentSlideNonNormalized
        else
            slidingView.x = currentSlideNonNormalized

        invalidate()
        if (state != previousState || state == PanelState.SLIDING) {
            notifyListeners(currentSlide)
            previousState = state
        }
    }

    /**
     * Sets the drag view. The drag view is the view from which the sliding panel can be dragged.
     */
    fun setDragView(dragView: View) {
        this.dragView = dragView
        dragViewId = dragView.id
    }

    /**
     * Use this method to change the state of the slidingView.
     */
    fun slideTo(state: PanelState) {
        if (state === this.state)
            return

        when (state) {
            PanelState.EXPANDED -> slideTo(1f)
            PanelState.COLLAPSED -> slideTo(0f)
            PanelState.SLIDING -> throw IllegalArgumentException("You are not allowed to set the state to SLIDING. Please use EXPANDED or COLLAPSED")
        }
    }

    /**
     * Toogles the state of the panel, between [PanelState.COLLAPSED] and [PanelState.EXPANDED]
     */
    fun toggle() {
        if (state === PanelState.EXPANDED) slideTo(PanelState.COLLAPSED) else slideTo(PanelState.EXPANDED)
    }

    fun addSlideListener(listener: OnSlideListener) {
        listeners.add(listener)
    }

    fun addSlideListener(callback: (slidingPanel: SlidingPanelLayout, state: PanelState, currentSlide: Float) -> Unit) {
        addSlideListener(object : OnSlideListener {
            override fun onSlide(
                slidingPanel: SlidingPanelLayout,
                state: PanelState,
                currentSlide: Float
            ) = callback(slidingPanel, state, currentSlide)
        })
    }

    fun removeListener(listener: OnSlideListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(currentSlide: Float) {
        for (listener in listeners)
            listener.onSlide(this, state, currentSlide)
    }

    /**
     * Implement this interface if you want to observe changes in the panel.
     */
    interface OnSlideListener {
        /**
         * This function is called every time the panel slides.
         * @param slidingPanel a reference to the panel that is being observed.
         * @param state the state of the panel. One of the states defined in [PanelState].
         * @param currentSlide the current slide value. It is a value between 0 ([PanelState.COLLAPSED]) and 1 ([PanelState.EXPANDED]).
         */
        fun onSlide(slidingPanel: SlidingPanelLayout, state: PanelState, currentSlide: Float)
    }
}
