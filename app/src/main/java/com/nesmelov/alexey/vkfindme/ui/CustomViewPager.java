package com.nesmelov.alexey.vkfindme.ui;

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
        this.mPagingEnabled = true;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (this.mPagingEnabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        if (this.mPagingEnabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    /**
     * Sets paging enabled.
     *
     * @param enabled paging enabling.
     */
    public void setPagingEnabled(final boolean enabled) {
        this.mPagingEnabled = enabled;
    }
}