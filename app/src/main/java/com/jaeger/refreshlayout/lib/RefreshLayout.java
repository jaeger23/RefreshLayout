package com.jaeger.refreshlayout.lib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by Jaeger on 15-7-22.
 */
public class RefreshLayout extends FrameLayout {

    private OnRefreshListener onRefreshListener;
    private View mChildView;
    private HeaderView mHeader;


    private boolean mIsRefreshing;
    private ValueAnimator mUpBackAnimator;
    private float mTouchStartY;
    private float mTouchCurY;
    private float PULL_HEIGHT;
    private int HEADER_HEIGHT;


    public RefreshLayout(Context context) {
        this(context, null, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }


    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        PULL_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
        HEADER_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics());
        if (getChildCount() > 1) {
            throw new RuntimeException("you can only attach one child");
        }
        this.post(new Runnable() {
            @Override
            public void run() {
                mChildView = getChildAt(0);
                addHeaderView();

            }
        });

    }

    private void addHeaderView() {
        mHeader = new HeaderView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.gravity = Gravity.TOP;
        mHeader.setLayoutParams(params);
        addView(mHeader);
        setUpBackAnimation();

    }

    private void setUpBackAnimation() {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsRefreshing) {
            return true;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchCurY = ev.getY();
                float dy = mTouchCurY - mTouchStartY;
                if (mChildView != null) {
                    if (dy > 0 && !ViewCompat.canScrollVertically(mChildView, -1)) {
                        return true;
                    }
                }
                return false;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsRefreshing){
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                mIsRefreshing = false;
                mTouchCurY = event.getY();
                float dy = mTouchCurY - mTouchStartY;
                dy = Math.min(PULL_HEIGHT * 2, dy);
                dy = Math.max(0, dy);
                if (mChildView != null) {
                    dy = new DecelerateInterpolator(10).getInterpolation(dy / PULL_HEIGHT / 2) * dy / 2;
                    mChildView.setTranslationY(dy);
                }
                mHeader.mTouchX = (int) event.getX();
                mHeader.getLayoutParams().height = (int) dy;
                mHeader.requestLayout();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mChildView != null) {
                    float height = mChildView.getTranslationY();
                    if (height > HEADER_HEIGHT) {
                        mIsRefreshing = true;
                        mUpBackAnimator = ValueAnimator.ofFloat(height, HEADER_HEIGHT);
                        mUpBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float val = (float) animation.getAnimatedValue();
                                mChildView.setTranslationY(val);
                                mHeader.getLayoutParams().height = (int) val;
                                mHeader.requestLayout();
                                if (val == HEADER_HEIGHT) {
                                    mHeader.startRefresh();
                                    if (onRefreshListener != null) {
                                        onRefreshListener.refreshing();
                                    }
                                }
                            }
                        });
                        mUpBackAnimator.setDuration(500);
                        mUpBackAnimator.start();
                    } else {
                        backToTopAnimator();
                    }
                }
        }

        return super.onTouchEvent(event);
    }

    public void finishRefreshing() {
        if (onRefreshListener != null) {
            onRefreshListener.completeRefresh();
        }
        if (mHeader != null) {
            mHeader.setIsRefresh(false);
        }
        mIsRefreshing = false;
        backToTopAnimator();

    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public interface OnRefreshListener {
        void refreshing();

        void completeRefresh();

    }

    private void backToTopAnimator() {
        mUpBackAnimator = ValueAnimator.ofFloat(mChildView.getTranslationY(), 0);
        mUpBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                mChildView.setTranslationY(val);
                mHeader.getLayoutParams().height = (int) val;
                mHeader.requestLayout();
            }
        });
        mUpBackAnimator.setDuration(500);
        mUpBackAnimator.start();
    }
}
