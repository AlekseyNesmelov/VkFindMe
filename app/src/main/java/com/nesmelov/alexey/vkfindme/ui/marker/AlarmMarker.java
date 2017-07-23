package com.nesmelov.alexey.vkfindme.ui.marker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.utils.Utils;

import java.util.ArrayList;

public class AlarmMarker extends Alarm {
    private static final int ALARM_SIZE_DP = 40;

    private Marker mMarker;
    private Circle mCircle;
    private int mColor;

    public AlarmMarker(final long alarmId, final double lat, final double lon, final float radius,
                       final int color) {
        super(alarmId, lat, lon, radius);
        mColor = color;
    }

    public AlarmMarker(final long alarmId, final double lat, final double lon, final float radius,
                 final int color, final ArrayList<Integer> users, final String names) {
        super(alarmId, lat, lon, radius, users, names);
        mColor = color;
    }

    public Marker addToMap(final Context context, final GoogleMap map) {
        final int size = Utils.dpToPx(context, ALARM_SIZE_DP);
        final Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.alarm_icon), size, size, false);
        final MarkerOptions markerOptions = new MarkerOptions()
                .title(context.getString(R.string.alarm))
                .snippet(mNames)
                .position(new LatLng(mLat, mLon))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .anchor(0.5f, 0.7f);
        mMarker = map.addMarker(markerOptions);

        mCircle = map.addCircle(new CircleOptions()
                .center(new LatLng(mLat, mLon))
                .visible(true)
                .fillColor(mColor)
                .strokeColor(Color.argb(96, 100, 100, 100))
                .strokeWidth(3)
                .radius(mRadius)
        );

        return mMarker;
    }

    public Circle getCircle() {
        return mCircle;
    }

    public Marker getMarker() {
        return mMarker;
    }
}
