package com.pierfrancescosoffritti.slidingdrawer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom View implementing a bottom sheet that is part of the view hierarchy, not above it.
 * <br/><br/>
 * This ViewGroup can have only 2 children:
 * <ol>
 * <li>The first children is the non slidable view (the view that will be covered when the bottom sheet is expanded).</li>
 * <li>The seconds children is the slidable view (the actual bottom sheet, the view that will be sliding over the non slidable view).</li>
 * </ol>
 */
public class SlidingDrawer extends LinearLayout {

    private static final int SLIDE_DURATION = 300;
    private final int TOUCH_SLOP = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    // the color of the shade that fades over the non slidable view when the slidable view slides
    private static final int SHADE_COLOR_WITH_ALPHA = 0x99000000;
    private static final int SHADE_COLOR_MAX_ALPHA = SHADE_COLOR_WITH_ALPHA >>> 24;
    private static final int SHADE_COLOR = SHADE_COLOR_WITH_ALPHA & 0x00FFFFFF;

    // view that will slide
    private View slidingView;

    // view that won't slide
    private View nonSlidingView;

    // the only view sensible to vertical dragging. Dragging this view will slide the slidingView
    private View dragView;

    // an optional view, child of slidingView, to which a margin bottom is added, in order to make it always fit in the screen.
    private View fittingView;

    private PanelState state = PanelState.COLLAPSED;
    // A value between 1.0 and 0.0 (1.0 = EXPANDED, 0.0 = COLLAPSED)
    private float currentSlide = 0.0f;

    // max and min values of the slide, non normalized
    private int nonSlidingViewHeight;
    private final int slidingViewDistanceFromParentTop = 0;

    // duration of the slide in milliseconds, when executed with an animation instead of a gesture
    private long slideDuration = SLIDE_DURATION;

    private boolean isSliding = false;
    private boolean canSlide = false;

    // distance between sliding view's edge and the initialTouchEventY coordinate
    private float touchYSlidingViewTopDelta;
    private float initialTouchEventY;

    private final Drawable elevationShadow;
    private int elevationShadowLength;

    private int slidingViewId;
    private int nonSlidingViewId;
    private int dragViewId;
    private int fittingViewId;

    private boolean fitSlidingContentToScreen;

    private final Set<OnSlideListener> listeners = new HashSet<>();

    public SlidingDrawer(Context context) {
        this(context, null);
    }

    public SlidingDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        applyAttributes(attrs);

        elevationShadow = ContextCompat.getDrawable(getContext(), R.drawable.elevation_shadow);
        setWillNotDraw(false);
    }

    private void applyAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SlidingDrawer, 0, 0);

        try {
            slidingViewId = typedArray.getResourceId(R.styleable.SlidingDrawer_slidingView, -1);
            nonSlidingViewId = typedArray.getResourceId(R.styleable.SlidingDrawer_nonSlidingView, -1);
            dragViewId = typedArray.getResourceId(R.styleable.SlidingDrawer_dragView, -1);
            fittingViewId = typedArray.getResourceId(R.styleable.SlidingDrawer_fitViewToScreen, -1);

            elevationShadowLength = typedArray.getDimensionPixelSize(R.styleable.SlidingDrawer_elevation, 10);
            fitSlidingContentToScreen = typedArray.getBoolean(R.styleable.SlidingDrawer_fitSlidingContentToScreen, true);
        } finally {
            typedArray.recycle();
        }

        if(slidingViewId == -1)
            throw new RuntimeException("SlidingPanel, app:slidingView attribute not set. You must set this attribute to the id of the view that you want to be sliding.");
        if(nonSlidingViewId == -1)
            throw new RuntimeException("SlidingPanel, app:nonSlidingViewId attribute not set. You must set this attribute to the id of the view that you want to be static.");
        if(fitSlidingContentToScreen && fittingViewId != -1)
            throw new RuntimeException("SlidingPanel, app:fitSlidingContentToScreen is set to true and app:fitViewToScreen is used." +
                    " This two attributes are mutually exclusive, you can use only one at a time.");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (getChildCount() != 2)
            throw new IllegalStateException("SlidingPanel must have exactly 2 children. But has " +getChildCount());

        slidingView = findViewById(slidingViewId);
        nonSlidingView = findViewById(nonSlidingViewId);
        dragView = findViewById(dragViewId);

        fittingView = findViewById(fittingViewId);

        if(dragView == null)
            dragView = slidingView;

        if(slidingView == null)
            throw new RuntimeException("SlidingPanel, slidingView is null.");
        if(nonSlidingView == null)
            throw new RuntimeException("SlidingPanel, nonSlidingView is null.");
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent touchEvent) {
        final int action = touchEvent.getAction();

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // stop sliding and let the child handle the touch event
            isSliding = false;
            canSlide = false;

            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // save touch coordinates
                float touchX = touchEvent.getRawX();
                float touchY = touchEvent.getRawY();

                final int[] viewCoordinates = new int[2];
                dragView.getLocationInWindow(viewCoordinates);

                final float viewX = viewCoordinates[0];
                final float viewWidth = dragView.getWidth();
                final float viewY = viewCoordinates[1];
                final float viewHeight = dragView.getHeight();

                // slidingView can slide only if the ACTION_DOWN event is within dragView bounds
                canSlide = !(touchX < viewX || touchX > viewX + viewWidth || touchY < viewY || touchY > viewY + viewHeight);

                if(!canSlide)
                    return false;

                initialTouchEventY = touchEvent.getY();
                touchYSlidingViewTopDelta = slidingView.getY() - initialTouchEventY;

                // intercept touch event only if sliding
                return isSliding;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isSliding)
                    return true;
                if(!canSlide)
                    return false;

                // start sliding only if the user dragged for more than TOUCH_SLOP
                final float diff = Math.abs(touchEvent.getY() - initialTouchEventY);

                if (diff > TOUCH_SLOP /2) {
                    // Start sliding
                    isSliding = true;
                    return true;
                }
                break;
            }
        }

        // In general, we don't want to intercept touch events. They should be handled by the child view.
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentTouchEventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if(isSliding && state != PanelState.EXPANDED && state != PanelState.COLLAPSED)
                    completeSlide(currentSlide, currentTouchEventY > this.initialTouchEventY ? SlidingDirection.DOWN : SlidingDirection.UP);

                canSlide = false;
                isSliding = false;
                break;
            case MotionEvent.ACTION_MOVE:

                if(!isSliding || !canSlide)
                    return false;

                float finalPositionY = currentTouchEventY + touchYSlidingViewTopDelta;

                if(finalPositionY > nonSlidingViewHeight)
                    finalPositionY = nonSlidingViewHeight;
                else if(finalPositionY < slidingViewDistanceFromParentTop)
                    finalPositionY = slidingViewDistanceFromParentTop;

                updateState(Utils.INSTANCE.normalize(finalPositionY, nonSlidingViewHeight));
                break;
        }
        return true;
    }

    private void completeSlide(float currentSlide, SlidingDirection direction) {
        float slidingViewTargetY;

        switch (direction) {
            case UP:
                if(currentSlide > 0.1)
                    slidingViewTargetY = slidingViewDistanceFromParentTop;
                else
                    slidingViewTargetY = nonSlidingViewHeight;
                break;
            case DOWN:
                if(currentSlide < 0.9)
                    slidingViewTargetY = nonSlidingViewHeight;
                else
                    slidingViewTargetY = slidingViewDistanceFromParentTop;
                break;
            default:
                return;
        }

        slideTo(Utils.INSTANCE.normalize(slidingViewTargetY, nonSlidingViewHeight));
    }

    /**
     * always use this method to update the position of the sliding view.
     * @param newSlideRatio new slide value, normalized between 0 and 1
     */
    private void updateState(float newSlideRatio) {
        if(newSlideRatio < 0 || newSlideRatio > 1)
            throw new IllegalArgumentException("Slide value \"" +newSlideRatio +"\" should be normalized, between 0 and 1.");

        currentSlide = newSlideRatio;

        state = currentSlide == 1 ? PanelState.EXPANDED : currentSlide == 0 ? PanelState.COLLAPSED : PanelState.SLIDING;

        float currentSlideNonNormalized = Math.abs((currentSlide * nonSlidingViewHeight) - nonSlidingViewHeight);

        slidingView.setY(currentSlideNonNormalized);
        invalidate();

        notifyListeners(currentSlide);
    }

    /**
     * Ask all children to measure themselves, then compute the measure of this layout based on its children.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        // Iterate through all children, measuring them while using their size to compute the dimensions of this view.
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;

            // Measure the child.
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

            // Update SlidingPanel size based on layout params of child.
            // Children that asked to be positioned on the left or right go in those gutters.
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);

            maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Report our final dimensions.
        setMeasuredDimension(
                resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(
                        maxHeight,
                        heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT
                )
        );
    }

    private final Rect tmpContainerRect = new Rect();
    private final Rect tmpChildRect = new Rect();

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int currentTop = getPaddingTop();
        int parentBottom = getPaddingBottom();
        int currentLeft = getPaddingLeft();
        int parentRight = getPaddingRight();

        for (int i=0; i<getChildCount(); i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            final LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();

            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();

            tmpContainerRect.left = currentLeft + childLayoutParams.leftMargin;
            tmpContainerRect.right = currentLeft + childWidth - childLayoutParams.rightMargin;

            tmpContainerRect.top = currentTop + childLayoutParams.topMargin;
            tmpContainerRect.bottom = currentTop + childHeight - childLayoutParams.bottomMargin;

            if(getOrientation() == VERTICAL)
                currentTop = tmpContainerRect.bottom;
            else
                currentLeft = tmpContainerRect.right;

            Gravity.apply(childLayoutParams.gravity, childWidth, childHeight, tmpContainerRect, tmpChildRect);

            child.layout(tmpChildRect.left, tmpChildRect.top, tmpChildRect.right, tmpChildRect.bottom);
        }
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);

        // draw the elevation shadow
        if (elevationShadow != null) {
            final int right = slidingView.getRight();
            final int top = (int) (slidingView.getY() - elevationShadowLength);
            final int bottom = (int) slidingView.getY();
            final int left = slidingView.getLeft();

            elevationShadow.setBounds(left, top, right, bottom);
            elevationShadow.draw(c);
        }
    }

    private final Rect tmpRect = new Rect();
    private final Paint coveredFadePaint = new Paint();

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result;
        final int save = canvas.save();

        if(child == nonSlidingView) {
            nonSlidingViewHeight = nonSlidingView.getHeight();

            // Clip against the slider; no sense drawing what will immediately be covered,
            // Unless the panel is set to overlay content
            canvas.getClipBounds(tmpRect);
            tmpRect.bottom = Math.min(tmpRect.bottom, slidingView.getTop());

            result = super.drawChild(canvas, child, drawingTime);

            if (currentSlide > 0) {
                final int currentAlpha = (int) (SHADE_COLOR_MAX_ALPHA * currentSlide);
                final int color = currentAlpha << 24 | SHADE_COLOR;
                coveredFadePaint.setColor(color);
                canvas.drawRect(tmpRect, coveredFadePaint);
            }
        } else if (child == slidingView) {
            applyFitToScreen();
            result = super.drawChild(canvas, child, drawingTime);
        } else {
            result = super.drawChild(canvas, child, drawingTime);
        }

        canvas.restoreToCount(save);

        return result;
    }

    boolean fitScreenApplied = false;
    private void applyFitToScreen() {
        if(fitScreenApplied) return;

        if (fitSlidingContentToScreen) {
            if (slidingView instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) slidingView).getChildCount(); i++)
                    Utils.INSTANCE.setBottomMargin(((ViewGroup) slidingView).getChildAt(i), nonSlidingViewHeight);
            } else {
                Utils.INSTANCE.setBottomPadding(slidingView, nonSlidingViewHeight);
            }
        } else if(fittingView != null) {
            Utils.INSTANCE.setBottomMargin(fittingView, nonSlidingViewHeight);
        }

        fitScreenApplied = true;
    }

    /**
     * Use this method in order to slide at a specific position
     * @param positionNormalized Normalized value (between 0.0 and 1.0) representing the final slide position
     */
    public void slideTo(float positionNormalized) {
        if(Float.isNaN(positionNormalized))
            throw new IllegalArgumentException("Bad value. Can't slide to NaN");
        if(positionNormalized < 0 || positionNormalized > 1)
            throw new IllegalArgumentException("Bad value. Can't slide to " +positionNormalized +". Value must be between 0 and 1");

        ValueAnimator va = ValueAnimator.ofFloat(currentSlide, positionNormalized);
        va.setInterpolator(new DecelerateInterpolator(1.5f));
        va.setDuration(slideDuration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateState((Float) animation.getAnimatedValue());
            }
        });
        va.start();
    }

    public void setDragView(@NonNull View dragView) {
        this.dragView = dragView;
    }

    /**
     * @return shadow height in pixels
     */
    public int getElevationShadowLength() {
        return elevationShadowLength;
    }

    /**
     * @param elevationShadowLength shadow height in pixels
     */
    public void setElevationShadowLength(int elevationShadowLength) {
        this.elevationShadowLength = elevationShadowLength;
    }

    public PanelState getState() {
        return state;
    }

    /**
     * Use this method to change the state of the slidingView.
     * @param state a value from {@link PanelState}
     */
    public void setState(PanelState state) {
        if(state == this.state)
            return;

        switch (state) {
            case EXPANDED:
                slideTo(1);
                break;
            case COLLAPSED:
                slideTo(0);
                break;
            case SLIDING:
                break;
        }
    }

    public long getSlideDuration() {
        return slideDuration;
    }

    /**
     * Set the duration of the automatic slide animation
     * @param slideDuration duration in milliseconds
     */
    public void setSlideDuration(long slideDuration) {
        this.slideDuration = slideDuration;
    }

    public void addSlideListener(@NonNull OnSlideListener listener) {
        listeners.add(listener);
    }

    public void removeListener(@NonNull OnSlideListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(float currentSlide) {
        for(OnSlideListener listener : listeners)
            listener.onSlide(this, currentSlide);
    }

    /**
     * Implement this interface if you want to observe slide changes
     */
    public interface OnSlideListener {
        void onSlide(SlidingDrawer slidingDrawer, float currentSlide);
    }
}
