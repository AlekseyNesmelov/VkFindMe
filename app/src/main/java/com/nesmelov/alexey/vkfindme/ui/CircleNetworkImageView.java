package com.nesmelov.alexey.vkfindme.ui;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;
import com.nesmelov.alexey.vkfindme.utils.Utils;

public class CircleNetworkImageView extends NetworkImageView {
    Context mContext;

    public CircleNetworkImageView(Context context) {
        super(context);
        mContext = context;
    }

    public CircleNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public CircleNetworkImageView(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if(bm==null) return;
        setImageDrawable(new BitmapDrawable(mContext.getResources(),
                Utils.getCroppedBitmap(bm)));
    }
}