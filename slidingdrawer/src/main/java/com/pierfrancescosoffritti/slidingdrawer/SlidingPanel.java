package com.pierfrancescosoffritti.slidingdrawer;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
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
public class SlidingPanel extends LinearLayout {

    private final int TOUCH_SLOP = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private static final int SLIDE_DURATION_SHORT = 300;
    private static final int SLIDE_DURATION_LONG = 600;

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

    // slidingView coordinate when expanded
    private int maxSlide;
    // slidingView coordinate when collapsed
    private final int minSlide = 0;

    // duration of the slide in milliseconds, when executed with an animation instead of a gesture
    private long slideDuration = SLIDE_DURATION_SHORT;

    private float slidingViewPositioOnTouchDown;
    private float initialTouchCoordinates;

    boolean isSliding = false;

    private final Drawable elevationShadow90Deg;
    private final Drawable elevationShadow180Deg;
    private int elevationShadowLength;

    private int slidingViewId;
    private int nonSlidingViewId;
    private int dragViewId;
    private int fittingViewId;

    private boolean fitSlidingContentToScreen;

    private final Set<OnSlideListener> listeners = new HashSet<>();

    public SlidingPanel(Context context) {
        this(context, null);
    }

    public SlidingPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        applyAttributes(attrs);

        elevationShadow90Deg = ContextCompat.getDrawable(getContext(), R.drawable.elevation_shadow_90_deg);
        elevationShadow180Deg = ContextCompat.getDrawable(getContext(), R.drawable.elevation_shadow_180_deg);
        setWillNotDraw(false);
    }

    private void applyAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SlidingPanel, 0, 0);

        try {
            slidingViewId = typedArray.getResourceId(R.styleable.SlidingPanel_slidingView, -1);
            nonSlidingViewId = typedArray.getResourceId(R.styleable.SlidingPanel_nonSlidingView, -1);
            dragViewId = typedArray.getResourceId(R.styleable.SlidingPanel_dragView, -1);
            fittingViewId = typedArray.getResourceId(R.styleable.SlidingPanel_fitViewToScreen, -1);

            elevationShadowLength = typedArray.getDimensionPixelSize(R.styleable.SlidingPanel_elevation, 10);
            fitSlidingContentToScreen = typedArray.getBoolean(R.styleable.SlidingPanel_fitSlidingContentToScreen, true);
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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (getChildCount() != 2)
            throw new IllegalStateException("SlidingPanel must have exactly 2 children. But has " +getChildCount());

        slidingView = findViewById(slidingViewId);
        nonSlidingView = findViewById(nonSlidingViewId);
        dragView = findViewById(dragViewId);

        fittingView = findViewById(fittingViewId);

        if(slidingView == null)
            throw new RuntimeException("SlidingPanel, slidingView is null.");
        if(nonSlidingView == null)
            throw new RuntimeException("SlidingPanel, nonSlidingView is null.");

        // optional views
        if(dragViewId != -1 && dragView == null)
            throw new RuntimeException("SlidingPanel, can't find dragView.");
        if(fittingViewId != -1 && fittingView == null)
            throw new RuntimeException("SlidingPanel, can't find fittingView.");

        if(dragView == null)
            dragView = slidingView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent currentTouchEvent) {
        if(!Utils.INSTANCE.isMotionEventWithinBoundaries(currentTouchEvent, dragView)) {
            isSliding = false;
            return false;
        }

        switch (currentTouchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                initialTouchCoordinates = getOrientation() == VERTICAL ? currentTouchEvent.getY() : currentTouchEvent.getX();
                slidingViewPositioOnTouchDown = getOrientation() == VERTICAL ? slidingView.getY() : slidingView.getX();
                return false;
            } case MotionEvent.ACTION_MOVE: {
                float currentTouch = getOrientation() == VERTICAL ? currentTouchEvent.getY() : currentTouchEvent.getX();
                final float diff = Math.abs(currentTouch - initialTouchCoordinates);

                isSliding = diff > TOUCH_SLOP/4;
                return isSliding;
            } default:
                isSliding = false;
                return false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent touchEvent) {
        float currentTouchEvent = getOrientation() == VERTICAL ? touchEvent.getY() : touchEvent.getX();

        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                if(state == PanelState.SLIDING)
                    completeSlide(currentSlide, currentTouchEvent > initialTouchCoordinates ? SlidingDirection.DOWN : SlidingDirection.UP);
                break;
            case MotionEvent.ACTION_MOVE:
                if(!isSliding && !Utils.INSTANCE.isMotionEventWithinBoundaries(touchEvent, dragView))
                    return false;

                float touchOffset = initialTouchCoordinates - currentTouchEvent;
                float finalPosition = slidingViewPositioOnTouchDown - touchOffset;

                if(finalPosition > maxSlide)
                    finalPosition = maxSlide;
                else if(finalPosition < minSlide)
                    finalPosition = minSlide;

                updateState(Utils.INSTANCE.normalize(finalPosition, maxSlide));
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();

        if(state == PanelState.EXPANDED) setState(PanelState.COLLAPSED); else setState(PanelState.EXPANDED);

        return true;
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
        int currentLeft = getPaddingLeft();

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
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(getOrientation() == VERTICAL) drawElevationShadow90(canvas); else drawElevationShadow180(canvas);
    }

    private void drawElevationShadow90(Canvas canvas) {
        final int top = (int) (slidingView.getY() - elevationShadowLength);
        final int bottom = (int) slidingView.getY();
        final int left = slidingView.getLeft();
        final int right = slidingView.getRight();

        elevationShadow90Deg.setBounds(left, top, right, bottom);
        elevationShadow90Deg.draw(canvas);
    }

    private void drawElevationShadow180(Canvas canvas) {
        final int top = slidingView.getTop();
        final int bottom = slidingView.getBottom();
        final int left = (int) (slidingView.getX() - elevationShadowLength);
        final int right = (int) slidingView.getX();

        elevationShadow180Deg.setBounds(left, top, right, bottom);
        elevationShadow180Deg.draw(canvas);
    }

    private final Rect tmpRect = new Rect();
    private final Paint shadePaint = new Paint();

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result;
        final int save = canvas.save();

        if(child == nonSlidingView) {
            maxSlide = getOrientation() == VERTICAL ? nonSlidingView.getHeight() : nonSlidingView.getWidth();

            // Clip against the slider; no sense drawing what will immediately be covered,
            // Unless the panel is set to overlay content
            canvas.getClipBounds(tmpRect);
            tmpRect.bottom = Math.min(tmpRect.bottom, slidingView.getTop());

            result = super.drawChild(canvas, child, drawingTime);

            if (currentSlide > 0) {
                final int currentShadeAlpha = (int) (SHADE_COLOR_MAX_ALPHA * currentSlide);
                final int currentShadeColor = currentShadeAlpha << 24 | SHADE_COLOR;
                shadePaint.setColor(currentShadeColor);
                canvas.drawRect(tmpRect, shadePaint);
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

        int side = getOrientation() == VERTICAL ? 3 : 2;
        if (fitSlidingContentToScreen) {
            if (slidingView instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) slidingView).getChildCount(); i++)
                    Utils.INSTANCE.setMargin(((ViewGroup) slidingView).getChildAt(i), maxSlide, side);
            } else {
                Utils.INSTANCE.setPadding(slidingView, maxSlide, side);
            }
        } else if(fittingView != null) {
            Utils.INSTANCE.setMargin(fittingView, maxSlide, side);
        }

        fitScreenApplied = true;
    }

    /**
     * Use this method to automatically slide at a specific position
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

    private void completeSlide(float currentSlide, SlidingDirection direction) {
        float targetSlide;

        switch (direction) {
            case UP:
                if(currentSlide > 0.1)
                    targetSlide = minSlide;
                else
                    targetSlide = maxSlide;
                break;
            case DOWN:
                if(currentSlide < 0.9)
                    targetSlide = maxSlide;
                else
                    targetSlide = minSlide;
                break;
            default:
                return;
        }

        slideTo(Utils.INSTANCE.normalize(targetSlide, maxSlide));
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

        float currentSlideNonNormalized = Math.abs((currentSlide * maxSlide) - maxSlide);
        if(getOrientation() == VERTICAL)
            slidingView.setY(currentSlideNonNormalized);
        else
            slidingView.setX(currentSlideNonNormalized);

        invalidate();
        notifyListeners(currentSlide);
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
        void onSlide(SlidingPanel slidingPanel, float currentSlide);
    }
}
