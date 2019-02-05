package com.pierfrancescosoffritti.slidingdrawer

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout

import com.pierfrancescosoffritti.slidingdrawer.utils.Utils
import com.pierfrancescosoffritti.slidingdrawer.utils.ViewUtils
import com.pierfrancescosoffritti.slidingdrawer.utils.Extensions.isOrientationVertical
import com.pierfrancescosoffritti.slidingdrawer.utils.Extensions.isMotionEventWithinBounds
import java.lang.Float.isNaN

import java.util.HashSet

/**
 * Custom View implementing a sliding panel (bottom sheet pattern) that is part of the view hierarchy, not above it.
 *
 * Read [detailed documentation here](https://github.com/PierfrancescoSoffritti/sliding-drawer)
 */
class SlidingPanel(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    companion object {
        const val  SLIDE_DURATION_SHORT = 300L
        const val  SLIDE_DURATION_LONG = 600L

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

    private var state = PanelState.COLLAPSED
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

    private var isSliding = false

    private val elevationShadow90Deg = ContextCompat.getDrawable(getContext(), R.drawable.elevation_shadow_90_deg)!!
    private val elevationShadow180Deg = ContextCompat.getDrawable(getContext(), R.drawable.elevation_shadow_180_deg)!!

    private val slidingViewId: Int
    private val nonSlidingViewId: Int
    private val fittingViewId: Int
    private var dragViewId: Int

    private val fitSlidingViewContentToScreen: Boolean
    private var fitScreenApplied = false

    private val listeners = HashSet<OnSlideListener>()

    /**
     * The duration of the slide in millisecond, when executed with an animation instead of a gesture
     */
    var slideDuration = SLIDE_DURATION_SHORT

    /**
     * Sliding panel shadow height in pixels
     */
    var elevationShadowLength: Int = 0

    init {
        setWillNotDraw(false)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SlidingPanel, 0, 0)

        try {
            slidingViewId = typedArray.getResourceId(R.styleable.SlidingPanel_slidingView, -1)
            nonSlidingViewId = typedArray.getResourceId(R.styleable.SlidingPanel_nonSlidingView, -1)
            dragViewId = typedArray.getResourceId(R.styleable.SlidingPanel_dragView, -1)
            fittingViewId = typedArray.getResourceId(R.styleable.SlidingPanel_fitViewToScreen, -1)

            elevationShadowLength = typedArray.getDimensionPixelSize(R.styleable.SlidingPanel_elevation, 10)
            fitSlidingViewContentToScreen = typedArray.getBoolean(R.styleable.SlidingPanel_fitSlidingContentToScreen, true)
        } finally {
            typedArray.recycle()
        }

        if (slidingViewId == -1)
            throw RuntimeException("SlidingPanel, app:slidingView attribute not set. You must set this attribute to the id of the view that you want to be sliding.")
        if (nonSlidingViewId == -1)
            throw RuntimeException("SlidingPanel, app:nonSlidingViewId attribute not set. You must set this attribute to the id of the view that you want to be static.")
        if (fitSlidingViewContentToScreen && fittingViewId != -1)
            throw RuntimeException(
                    "SlidingPanel, app:fitSlidingViewContentToScreen is set to true and app:fitViewToScreen is used."
                            +" This two attributes are mutually exclusive, you can use only one at a time."
            )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (childCount != 2)
            throw IllegalStateException("SlidingPanel must have exactly 2 children. But has $childCount")

        slidingView = findViewById(slidingViewId) ?: throw RuntimeException("SlidingPanel, slidingView is null.")
        nonSlidingView = findViewById(nonSlidingViewId) ?: throw RuntimeException("SlidingPanel, nonSlidingView is null.")

        dragView = findViewById(dragViewId) ?:
                if (dragViewId != -1) throw RuntimeException("SlidingPanel, can't find dragView.")
                else slidingView

        fittingView = findViewById(fittingViewId) ?:
                if(fittingViewId != -1) throw RuntimeException("SlidingPanel, can't find fittingView.")
                else null
    }

    override fun onInterceptTouchEvent(currentTouchEvent: MotionEvent): Boolean {
        if (!dragView.isMotionEventWithinBounds(currentTouchEvent)) {
            isSliding = false
            return false
        }

        when (currentTouchEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                coordOfFirstTouch = if (isOrientationVertical()) currentTouchEvent.y else currentTouchEvent.x
                slidingViewPosAtFirstTouch = if (isOrientationVertical()) slidingView.y else slidingView.x
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val currentTouch: Float = if (isOrientationVertical()) currentTouchEvent.y else currentTouchEvent.x
                val diff: Float = Math.abs(currentTouch - coordOfFirstTouch)
                isSliding = diff > touchSlop / 4
                return isSliding
            }
            else -> {
                isSliding = false
                return false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        val currentTouchEvent = if (isOrientationVertical()) touchEvent.y else touchEvent.x

        when (touchEvent.action) {
            MotionEvent.ACTION_UP ->
                if (state == PanelState.SLIDING)
                    completeSlide(currentSlide, if (currentTouchEvent > coordOfFirstTouch) SlidingDirection.DOWN else SlidingDirection.UP)
            MotionEvent.ACTION_MOVE -> {
                if (!isSliding && !dragView.isMotionEventWithinBounds(touchEvent))
                    return false

                val touchOffset: Float = coordOfFirstTouch - currentTouchEvent
                var finalPosition: Float = slidingViewPosAtFirstTouch - touchOffset

                finalPosition = Utils.clamp(finalPosition, minSlide, maxSlide)
                updateState(Utils.normalizeScreenCoordinate(finalPosition, maxSlide))
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        toggleState()
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

            val layoutParams = child.layoutParams as LinearLayout.LayoutParams

            slidingPanelWidth = Math.max(slidingPanelWidth, child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin)
            slidingPanelHeight = Math.max(slidingPanelHeight, child.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin)

            childrenCombinedMeasuredStates = View.combineMeasuredStates(childrenCombinedMeasuredStates, child.measuredState)
        }

        slidingPanelHeight = Math.max(slidingPanelHeight, suggestedMinimumHeight)
        slidingPanelWidth = Math.max(slidingPanelWidth, suggestedMinimumWidth)

        setMeasuredDimension(
                View.resolveSizeAndState(slidingPanelWidth, widthMeasureSpec, childrenCombinedMeasuredStates),
                View.resolveSizeAndState(slidingPanelHeight, heightMeasureSpec, childrenCombinedMeasuredStates)
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

            val childLayoutParams = child.layoutParams as LinearLayout.LayoutParams

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

            Gravity.apply(childLayoutParams.gravity, childWidth, childHeight, onLayoutContainerRect, onLayoutChildRect)

            child.layout(onLayoutChildRect.left, onLayoutChildRect.top, onLayoutChildRect.right, onLayoutChildRect.bottom)
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
        val top = (slidingView.y - elevationShadowLength).toInt()
        val bottom = slidingView.y.toInt()
        val left = slidingView.left
        val right = slidingView.right

        elevationShadow90Deg.setBounds(left, top, right, bottom)
        elevationShadow90Deg.draw(canvas)
    }

    private fun drawElevationShadow180(canvas: Canvas) {
        val top = slidingView.top
        val bottom = slidingView.bottom
        val left = (slidingView.x - elevationShadowLength).toInt()
        val right = slidingView.x.toInt()

        elevationShadow180Deg.setBounds(left, top, right, bottom)
        elevationShadow180Deg.draw(canvas)
    }

    private val drawChildChildTempRect = Rect()
    private val shadePaint = Paint()

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val save = canvas.save()
        val result: Boolean

        if (child === nonSlidingView) {
            maxSlide = if (isOrientationVertical()) nonSlidingView.height.toFloat() else nonSlidingView.width.toFloat()
            minSlide = if (isOrientationVertical()) nonSlidingView.top.toFloat() else nonSlidingView.right.toFloat()

            canvas.getClipBounds(drawChildChildTempRect)
            result = super.drawChild(canvas, child, drawingTime)

            if (currentSlide > 0) {
                val currentShadeAlpha = (SHADE_COLOR_MAX_ALPHA * currentSlide).toInt()
                val currentShadeColor = currentShadeAlpha shl 24 or SHADE_COLOR
                shadePaint.color = currentShadeColor
                canvas.drawRect(drawChildChildTempRect, shadePaint)
            }
        } else if (child == slidingView) {
            applyFitToScreenOnce()
            result = super.drawChild(canvas, child, drawingTime)
        } else {
            result = super.drawChild(canvas, child, drawingTime)
        }

        canvas.restoreToCount(save)
        return result
    }

    private fun applyFitToScreenOnce() {
        if (fitScreenApplied) return

        val side = if (isOrientationVertical()) Side.BOTTOM else Side.RIGHT
        if (fitSlidingViewContentToScreen) {
            if (slidingView is ViewGroup) {
                for (i: Int in 0 until (slidingView as ViewGroup).childCount)
                    ViewUtils.setMargin((slidingView as ViewGroup).getChildAt(i), maxSlide.toInt(), side)
            } else {
                ViewUtils.setPadding(slidingView, maxSlide.toInt(), side)
            }
        } else if (fittingView != null) {
            ViewUtils.setMargin(fittingView!!, maxSlide.toInt(), side)
        }

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

    private fun completeSlide(currentSlide: Float, direction: SlidingDirection) {
        val targetSlide: Float = when (direction) {
            SlidingDirection.UP -> if (currentSlide > 0.1)
                minSlide
            else
                maxSlide
            SlidingDirection.DOWN -> if (currentSlide < 0.9)
                maxSlide
            else
                minSlide
            else -> return
        }

        slideTo(Utils.normalizeScreenCoordinate(targetSlide, maxSlide))
    }

    /**
     * always use this method to update the position of the sliding view.
     * @param newSlideRatio new slide value, normalized between 0 and 1
     */
    private fun updateState(newSlideRatio: Float) {
        if (newSlideRatio < 0 || newSlideRatio > 1)
            throw IllegalArgumentException("Slide value \"$newSlideRatio\" should be normalized, between 0 and 1.")

        currentSlide = newSlideRatio
        state = if (currentSlide == 1f) PanelState.EXPANDED else if (currentSlide == 0f) PanelState.COLLAPSED else PanelState.SLIDING

        val currentSlideNonNormalized = Math.abs(currentSlide * maxSlide - maxSlide)
        if (isOrientationVertical())
            slidingView.y = currentSlideNonNormalized
        else
            slidingView.x = currentSlideNonNormalized

        invalidate()
        notifyListeners(currentSlide)
    }

    fun setDragView(dragView: View) {
        this.dragView = dragView
        dragViewId = dragView.id
    }

    fun getState(): PanelState {
        return state
    }

    /**
     * Use this method to change the state of the slidingView.
     */
    fun setState(state: PanelState) {
        if (state === this.state)
            return

        when (state) {
            PanelState.EXPANDED -> slideTo(1f)
            PanelState.COLLAPSED -> slideTo(0f)
            PanelState.SLIDING -> {
            }
        }
    }

    fun toggleState() {
        if (state === PanelState.EXPANDED) setState(PanelState.COLLAPSED) else setState(PanelState.EXPANDED)
    }

    fun addSlideListener(listener: OnSlideListener) {
        listeners.add(listener)
    }

    fun addSlideListener(callback: (slidingPanel: SlidingPanel, state: PanelState, currentSlide: Float) -> Unit) {
        addSlideListener(object : OnSlideListener {
            override fun onSlide(slidingPanel: SlidingPanel, state: PanelState, currentSlide: Float) = callback(slidingPanel, state, currentSlide)
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
        fun onSlide(slidingPanel: SlidingPanel, state: PanelState, currentSlide: Float)
    }
}