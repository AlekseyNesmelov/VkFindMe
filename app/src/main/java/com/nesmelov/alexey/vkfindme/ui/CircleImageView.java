package com.nesmelov.alexey.vkfindme.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.nesmelov.alexey.vkfindme.utils.Utils;

public class CircleImageView extends android.support.v7.widget.AppCompatImageView {
    Context mContext;

    public CircleImageView(final Context context) {
        super(context);
        mContext = context;
    }

    public CircleImageView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

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