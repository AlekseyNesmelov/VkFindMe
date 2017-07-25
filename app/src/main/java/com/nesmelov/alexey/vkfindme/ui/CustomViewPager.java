package com.nesmelov.alexey.vkfindme.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {

    private boolean mPaddingEnabled;

    public CustomViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.mPaddingEnabled = true;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (this.mPaddingEnabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        if (this.mPaddingEnabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setPagingEnabled(final boolean enabled) {
        this.mPaddingEnabled = enabled;
    }
}