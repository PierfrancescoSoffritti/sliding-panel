package com.pierfrancescosoffritti.slidingdrawer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

public class SlidingDrawer extends LinearLayout {

    private final ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
    private final int touchSlop = viewConfiguration.getScaledTouchSlop();

    private View slidingView;

    private float maxSlide;
    private boolean isSliding;
    private boolean canSlide = false;
    private float currentSlide;

    private float dY;

    private @IdRes int draggableViewID;
    private View draggableView;

    public void setDraggableView(View draggableView) {
        this.draggableView = draggableView;
    }

    public void setDraggableViewID(@IdRes int draggableViewID) {
        this.draggableViewID = draggableViewID;
    }

    public SlidingDrawer(Context context) {
        super(context);
    }

    public SlidingDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

//        if(draggableView == null)
//            draggableView = findViewById(draggableViewID);

//        System.out.println(draggableView);
//        System.out.println(draggableViewID);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private float yDown;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        final int action = MotionEventCompat.getActionMasked(event);

        // stop sliding and let the child handle the touch event
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {

//            if (!isSliding && canSlide)
//                completeSlide(currentSlide == 0 ? 1 : 0, 0);

            isSliding = false;
            canSlide = false;

            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // save touch coordinates
                float xDown = event.getRawX();
                yDown = event.getRawY();

                int[] coords = new int[2];
                draggableView.getLocationInWindow(coords);

                final float viewX = coords[0];
                final float viewWidth = draggableView.getWidth();
                final float viewY = coords[1];
                final float viewHeight = draggableView.getHeight();

                // slidingView can slide only if the ACTION_DOWN event is within its bounds
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
                if(isSliding)
                    completeSlide(currentSlide, eventY > yDown ? 1 : -1);

                isSliding = false;

                break;
            case MotionEvent.ACTION_MOVE:

                if(!isSliding || !canSlide)
                    return false;

                if(eventY + dY > maxSlide)
                    dY = maxSlide - eventY;
                else if(eventY + dY < 0)
                    dY = -eventY;

                // currentSlideNormalized : x = 1 : maxSliding
                currentSlide = Math.abs(eventY + dY - maxSlide);
                currentSlide = currentSlide / maxSlide;

                slidingView.setY(eventY + dY);

                break;
        }

        return true;
    }

    private void completeSlide(float currentSlide, int direction) {
        int finalY = -1;

        if(direction == -1 && currentSlide > 0.15) {
            finalY = 0;
            this.currentSlide = 1;
        } else if(direction == -1) {
            finalY = (int) maxSlide;
            this.currentSlide = 0;
        }

        if(direction == 1 && currentSlide < 0.85) {
            finalY = (int) maxSlide;
            this.currentSlide = 0;
        } else if(direction == 1) {
            finalY = 0;
            this.currentSlide = 1;
        }

        if(direction == 0 && currentSlide == 1) {
            finalY = 0;
            this.currentSlide = 1;
        } else if(direction == 0 && currentSlide == 0) {
            finalY = (int) maxSlide;
            this.currentSlide = 0;
        }

        if(finalY != -1)
            slidingView.animate()
                    .y(finalY)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setDuration(300)
                    .start();
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

    public interface onSlideListener {
        void onExpanded();
        void onCollapsed();
        void onSliding();
    }
}
