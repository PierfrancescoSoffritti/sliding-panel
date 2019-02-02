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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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

    private PanelState state = PanelState.COLLAPSED;
    // A value between 1.0 and 0.0 (1.0 = EXPANDED, 0.0 = COLLAPSED)
    private float currentSlide = 0.0f;

    // max and min values of the slide, non normalized
    private int maxSlide;
    private final int minSlide = 0;

    // duration of the slide in milliseconds, when executed with an animation instead of a gesture
    private long slideDuration = SLIDE_DURATION;

    private boolean isSliding = false;
    private boolean canSlide = false;

    // distance between the view's edge and the yDown coordinate
    private float dY;
    private float yDown;

    private final Drawable elevationShadow;
    private int elevationShadowLength;

    private int slidingViewId;
    private int nonSlidingViewId;

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
            elevationShadowLength = typedArray.getDimensionPixelSize(R.styleable.SlidingDrawer_elevation, 10);
            slidingViewId = typedArray.getResourceId(R.styleable.SlidingDrawer_slidingView, -1);
            nonSlidingViewId = typedArray.getResourceId(R.styleable.SlidingDrawer_nonSlidingView, -1);
        } finally {
            typedArray.recycle();
        }

        if(slidingViewId == -1)
            throw new RuntimeException("SlidingPanel, app:slidingView attribute not set. You must set this attribute to the id of the view that you want to be sliding.");
        if(nonSlidingViewId == -1)
            throw new RuntimeException("SlidingPanel, app:nonSlidingViewId attribute not set. You must set this attribute to the id of the view that you want to be static.");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        slidingView = findViewById(slidingViewId);
        nonSlidingView = findViewById(nonSlidingViewId);

        if(slidingView == null)
            throw new RuntimeException("SlidingPanel, slidingView is null.");
        if(nonSlidingView == null)
            throw new RuntimeException("SlidingPanel, nonSlidingView is null.");
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(dragView == null)
            throw new IllegalStateException("DragView is null. SlidingPanel requires you to always set a DragView manually, call SlidingPanel.setDragView");

        final int action = event.getAction();

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // stop sliding and let the child handle the touch event
            isSliding = false;
            canSlide = false;

            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // save touch coordinates
                float rawXDown = event.getRawX();
                float rawYDown = event.getRawY();

                final int[] viewCoordinates = new int[2];
                dragView.getLocationInWindow(viewCoordinates);

                final float viewX = viewCoordinates[0];
                final float viewWidth = dragView.getWidth();
                final float viewY = viewCoordinates[1];
                final float viewHeight = dragView.getHeight();

                // slidingView can slide only if the ACTION_DOWN event is within dragView bounds
                canSlide = !(rawXDown < viewX || rawXDown > viewX + viewWidth || rawYDown < viewY || rawYDown > viewY + viewHeight);

                if(!canSlide)
                    return false;

                yDown = event.getY();
                dY = slidingView.getY() - yDown;

                // intercept touch event only if sliding
                return isSliding;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isSliding)
                    return true;
                if(!canSlide)
                    return false;

                // start sliding only if the user dragged for more than TOUCH_SLOP
                final float diff = Math.abs(event.getY() - yDown);

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
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if(isSliding && state != PanelState.EXPANDED && state != PanelState.COLLAPSED)
                    // complete the slide if it's not completed yet.
                    completeSlide(currentSlide, eventY > yDown ? SlidingDirection.DOWN : SlidingDirection.UP);

                canSlide = false;
                isSliding = false;
                break;
            case MotionEvent.ACTION_MOVE:

                if(!isSliding || !canSlide)
                    return false;

                // stay within the bounds (maxSlide and 0)
                if(eventY + dY > maxSlide)
                    dY = maxSlide - eventY;
                else if(eventY + dY < minSlide)
                    dY = -eventY;

                updateState(Utils.INSTANCE.normalize(eventY + dY, maxSlide));
                break;
        }
        return true;
    }

    private void completeSlide(float currentSlide, SlidingDirection direction) {
        float finalY;

        switch (direction) {
            case UP:
                if(currentSlide > 0.1)
                    finalY = minSlide;
                else
                    finalY = maxSlide;
                break;
            case DOWN:
                if(currentSlide < 0.9)
                    finalY = maxSlide;
                else
                    finalY = minSlide;
                break;
                // do i need this?
//            case NONE:
//                // handle the single touch scenario
//                if(currentSlide == 1)
//                    finalY = minSlide;
//                else if(currentSlide == 0)
//                    finalY = maxSlide;
//                break;
            default:
                return;
        }

        slideTo(Utils.INSTANCE.normalize(finalY, maxSlide));
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

        slidingView.setY(currentSlideNonNormalized);
        invalidate();

        notifyListeners(currentSlide);
    }

    /**
     * Ask all children to measure themselves, then compute the measure of this layout based on its children.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        if (count != 2)
            throw new IllegalStateException("SlidingDrawer must have exactly 2 children, non_slidable_view and slidable_view.");

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        // Iterate through all children, measuring them while computing the dimensions of this view from their size.
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;

            // Measure the child.
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

            // Update our size information based on the layout params.
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
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    private final Rect tmpContainerRect = new Rect();
    private final Rect tmpChildRect = new Rect();

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int firstChildHeight = 0;

        int parentTop = getPaddingTop();
        int parentBottom = getPaddingBottom();
        int parentLeft = getPaddingLeft();
        int parentRight = getPaddingRight();

        for (int i=0; i<2; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            tmpContainerRect.left = parentLeft + lp.leftMargin;
            tmpContainerRect.right = width - lp.rightMargin - parentRight;

            tmpContainerRect.top = parentTop + lp.topMargin;
            tmpContainerRect.bottom = parentTop + height - lp.bottomMargin - parentBottom;

            if(i==0)
                firstChildHeight = child.getMeasuredHeight();
            else if(i==1)
                tmpContainerRect.bottom += firstChildHeight;

            parentTop = tmpContainerRect.bottom;

            Gravity.apply(lp.gravity, width, height, tmpContainerRect, tmpChildRect);

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
            maxSlide = nonSlidingView.getHeight();

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
            addPaddingToCollapsedView();
            result = super.drawChild(canvas, child, drawingTime);
        } else {
            result = super.drawChild(canvas, child, drawingTime);
        }

        canvas.restoreToCount(save);

        return result;
    }

    private void addPaddingToCollapsedView() {
        // the collapsed view is the view shown in the slidingView when collapsed.
        // it's important to add padding at the bottom, otherwise some content will be offscreen
        View collapsedView = slidingView.findViewById(R.id.sliding_drawer_collapsed_view);
        if(collapsedView != null)
            Utils.INSTANCE.setPaddingBottom(collapsedView, maxSlide);
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
