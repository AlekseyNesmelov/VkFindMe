package com.nesmelov.alexey.vkfindme.ui;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nesmelov.alexey.vkfindme.R;

import java.util.ArrayList;

public class AlarmMarker {
    private long mAlarmId;
    private double mLat;
    private double mLon;
    private float mRadius;
    private ArrayList<Integer> mUsers;
    private String mNames;
    private Marker mMarker;

    public AlarmMarker(final Context context, final long alarmId, final double lat, final double lon,
                       final float radius, final ArrayList<Integer> users,
                       final String names, final GoogleMap map) {
        mAlarmId = alarmId;
        mLat = lat;
        mLon = lon;
        mRadius = radius;
        mUsers = users;
        mNames = names;
        final MarkerOptions markerOptions = new MarkerOptions()
            .title(context.getString(R.string.alarm))
            .snippet(names)
            .position(new LatLng(lat, lon))
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.alarm_icon))
                .anchor(0.5f, 0.5f);
        mMarker = map.addMarker(markerOptions);
    }

    public AlarmMarker(final long alarmId, final double lat, final double lon,
                       final float radius, final ArrayList<Integer> users,
                       final String names) {
        mAlarmId = alarmId;
        mLat = lat;
        mLon = lon;
        mRadius = radius;
        mUsers = users;
        mNames = names;
    }

    public void setUsers(final ArrayList<Integer> users) {
        mUsers = users;
    }

    public long getAlarmId() {
        return mAlarmId;
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    public float getRadius() {
        return mRadius;
    }

    public ArrayList<Integer> getUsers() {
        return mUsers;
    }

    public String getNames() {
        return mNames;
    }

    public Marker getMarker() {
        return mMarker;
    }
}
