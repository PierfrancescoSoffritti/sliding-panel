package com.pierfrancescosoffritti.slidingdrawer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SlidingDrawer extends LinearLayout {

    private static final byte UP = 0;
    private static final byte DOWN = 1;
    private static final byte NONE = 2;
    @IntDef({UP, DOWN, NONE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface SlidingDirection {}

    public static final byte EXPANDED = 0;
    public static final byte COLLAPSED = 1;
    public static final byte SLIDING = 2;
    @IntDef({EXPANDED, COLLAPSED, SLIDING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {}

    private @State byte state = COLLAPSED;

    private final ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
    private final int touchSlop = viewConfiguration.getScaledTouchSlop();

    // view that will slide
    private View slidingView;

    // current slide value, between 1.0 and 0.0 (1.0 = EXPANDED, 0.0 = COLLAPSED)
    private float currentSlide;

    // only view sensible to vertical dragging
    private View draggableView;

    // max value by which sliding view can slide.
    private int maxSlide;
    private final int minSlide = 0;

    private boolean isSliding;
    private boolean canSlide = false;

    // distance between the view's edge and the yDown coordinate
    private float dY;
    private float yDown;

    private final Drawable shadowDrawable;

    private int shadowHeight = 10;

    public SlidingDrawer(Context context) {
        this(context, null);
    }

    public SlidingDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        shadowDrawable = ContextCompat.getDrawable(getContext(), R.drawable.above_shadow);

        setWillNotDraw(false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        final int action = MotionEventCompat.getActionMasked(event);

        // stop sliding and let the child handle the touch event
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            isSliding = false;
            canSlide = false;

            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // save touch coordinates
                float xDown = event.getRawX();
                yDown = event.getRawY();

                final int[] viewCoordinates = new int[2];
                draggableView.getLocationInWindow(viewCoordinates);

                final float viewX = viewCoordinates[0];
                final float viewWidth = draggableView.getWidth();
                final float viewY = viewCoordinates[1];
                final float viewHeight = draggableView.getHeight();

                // slidingView can slide only if the ACTION_DOWN event is within draggableView bounds
                canSlide = !(xDown < viewX || xDown > viewX + viewWidth || yDown < viewY || yDown > viewY + viewHeight);

                if(!canSlide)
                    return false;

                yDown = event.getY();

                dY = slidingView.getY() - yDown;

                // intercept only if sliding
                return isSliding;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isSliding)
                    return true;
                if(!canSlide)
                    return false;

                // start sliding only if the user dragged for more than touchSlop
                final float diff = Math.abs(event.getY() - yDown);

                if (diff > touchSlop/2) {
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
                if(isSliding && state != EXPANDED && state != COLLAPSED)
                    completeSlide(currentSlide, eventY > yDown ? DOWN : UP);

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

                updateSliding(normalizeSlide(eventY + dY));
                break;
        }
        return true;
    }

    private void completeSlide(float currentSlide, @SlidingDirection int direction) {
        float finalY = -1;

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
            // handle the single touch scenario
            case NONE:
                if(currentSlide == 1)
                    finalY = minSlide;
                else if(currentSlide == 0)
                    finalY = maxSlide;
                break;
        }

        if(finalY == -1)
            return;

        slideTo(normalizeSlide(finalY));
    }

    private float normalizeSlide(float currentY) {
        // currentSlide_Normalized : x = 1 : maxSliding
        return Math.abs(currentY - maxSlide) / maxSlide;
    }

    /**
     * always use this method to update the position of the sliding view.
     * @param newPositionNormalized new view position, normalized
     */
    private void updateSliding(float newPositionNormalized) {
        currentSlide = newPositionNormalized;

        state = currentSlide == 1 ? EXPANDED : currentSlide == 0 ? COLLAPSED : SLIDING;

        float slideY = Math.abs((currentSlide * maxSlide) - maxSlide);

        slidingView.setY(slideY);
        invalidate();
    }

    /**
     * Ask all children to measure themselves and compute the measurement of this
     * layout based on the children.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        if (count != 2)
            throw new IllegalStateException("SlidingDrawer must have exactly 2 children.");

        initSlidingChild();

        // Measurement will ultimately be computing these values.
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        // Iterate through all children, measuring them and computing our dimensions
        // from their size.
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;

            // Measure the child.
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

            // Update our size information based on the layout params.  Children
            // that asked to be positioned on the left or right go in those gutters.
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

    private void initSlidingChild() {
        slidingView = getChildAt(1);
        slidingView.setClickable(true);

        maxSlide = getChildAt(0).getHeight();
    }

    Rect mTmpContainerRect = new Rect();
    Rect mTmpChildRect = new Rect();

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

            mTmpContainerRect.left = parentLeft + lp.leftMargin;
            mTmpContainerRect.right = width - lp.rightMargin - parentRight;

            mTmpContainerRect.top = parentTop + lp.topMargin;
            mTmpContainerRect.bottom = parentTop + height - lp.bottomMargin - parentBottom;

            if(i==0)
                firstChildHeight = child.getMeasuredHeight();
            else if(i==1)
                mTmpContainerRect.bottom += firstChildHeight;

            parentTop = mTmpContainerRect.bottom;

            Gravity.apply(lp.gravity, width, height, mTmpContainerRect, mTmpChildRect);

            child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
        }
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);
System.out.println("invalidate");
        // draw the shadow
        if (shadowDrawable != null) {
            final int right = slidingView.getRight();
            final int top = (int) (slidingView.getY() - shadowHeight);
            final int bottom = (int) slidingView.getY();
            final int left = slidingView.getLeft();

            shadowDrawable.setBounds(left, top, right, bottom);
            shadowDrawable.draw(c);
        }
    }

    /**
     * Use this method in order to slide at a specific position
     * @param finalYNormalized Normalized value (between 0.0 and 1.0) representing the final slide position
     */
    public void slideTo(float finalYNormalized) {
        ValueAnimator va = ValueAnimator.ofFloat(currentSlide, finalYNormalized);
        va.setInterpolator(new DecelerateInterpolator(1.5f));
        va.setDuration(300);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateSliding((Float) animation.getAnimatedValue());
            }
        });
        va.start();
    }

    public void setDraggableView(View draggableView) {
        this.draggableView = draggableView;
    }

    /**
     * @return shadow height in pixels
     */
    public int getShadowHeight() {
        return shadowHeight;
    }

    /**
     * @param shadowHeight shadow height in pixels
     */
    public void setShadowHeight(int shadowHeight) {
        this.shadowHeight = shadowHeight;
    }

    public interface onSlideListener {
        void onExpanded();
        void onCollapsed();
        void onSliding();
    }
}
