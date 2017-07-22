package com.nesmelov.alexey.vkfindme.ui.marker;

import android.content.Context;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.structures.Alarm;

import java.util.ArrayList;

public class AlarmMarker extends Alarm {
    private Marker mMarker;

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
        return mMarker;
    }

    public Marker getMarker() {
        return mMarker;
    }
}
