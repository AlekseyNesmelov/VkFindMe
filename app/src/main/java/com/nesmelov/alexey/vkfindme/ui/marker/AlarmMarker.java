package com.nesmelov.alexey.vkfindme.ui.marker;

import android.content.Context;
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

import java.util.ArrayList;

public class AlarmMarker extends Alarm {
    private Marker mMarker;
    private Circle mCircle;

    public AlarmMarker(final long alarmId, final double lat, final double lon, final float radius) {
        super(alarmId, lat, lon, radius);
    }

    public AlarmMarker(final long alarmId, final double lat, final double lon, final float radius,
                 final ArrayList<Integer> users, final String names) {
        super(alarmId, lat, lon, radius, users, names);
    }

    public Marker addToMap(final Context context, final GoogleMap map) {
        final MarkerOptions markerOptions = new MarkerOptions()
                .title(context.getString(R.string.alarm))
                .snippet(mNames)
                .position(new LatLng(mLat, mLon))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.alarm_icon))
                .anchor(0.5f, 0.5f);
        mMarker = map.addMarker(markerOptions);

        mCircle = map.addCircle(new CircleOptions()
                .center(new LatLng(mLat, mLon))
                .visible(true)
                .fillColor(Color.argb(128, 180, 217, 242))
                .strokeColor(Color.argb(128, 118, 176, 215))
                .strokeWidth(5)
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
