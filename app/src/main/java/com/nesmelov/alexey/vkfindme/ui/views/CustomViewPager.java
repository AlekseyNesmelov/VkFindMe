package com.nesmelov.alexey.vkfindme.ui.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Custom view pager that allows to manage paging.
 */
public class CustomViewPager extends ViewPager {

    private boolean mPagingEnabled;

    /**
     * Constructs custom view pager.
     *
     * @param context context to use.
     * @param attrs attribute set.
     */
    public CustomViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mPagingEnabled = true;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return mPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        return mPagingEnabled && super.onInterceptTouchEvent(event);
    }

    /**
     * Sets paging enabled.
     *
     * @param enabled paging enabling.
     */
    public void setPagingEnabled(final boolean enabled) {
        mPagingEnabled = enabled;
    }
}