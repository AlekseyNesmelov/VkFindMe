package com.nesmelov.alexey.vkfindme.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.nesmelov.alexey.vkfindme.utils.Utils;

/**
 * Circle image view class.
 */
public class CircleImageView extends android.support.v7.widget.AppCompatImageView {
    private Context mContext;

    /**
     * Constructs circle image view.
     *
     * @param context context to use.
     */
    public CircleImageView(final Context context) {
        super(context);
        mContext = context;
    }

    /**
     * Constructs circle image view.
     *
     * @param context context to use.
     * @param attrs attribute set.
     */
    public CircleImageView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    /**
     * Constructs circle image view.
     *
     * @param context context to use.
     * @param attrs attribute set.
     * @param defStyle defStyleAttr.
     */
    public CircleImageView(final Context context, final AttributeSet attrs,
                           final int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    public void setImageBitmap(final Bitmap bm) {
        if(bm == null) return;
        setImageDrawable(new BitmapDrawable(mContext.getResources(),
                Utils.getCroppedBitmap(bm)));
    }
}