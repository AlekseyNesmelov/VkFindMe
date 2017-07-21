package com.nesmelov.alexey.vkfindme.ui.marker;

import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class UserMarker {
    private long mUserId;
    private String mMarkerId;
    private double mLat;
    private double mLon;
    private String mName;
    private String mSurname;
    private Marker mMarker;
    final MarkerOptions mMarkerOptions;

    public UserMarker(final long userId, final double lat, final double lon, final boolean visible,
                      final String name, final String surname, final Bitmap bitmap) {
        mUserId = userId;
        mLat = lat;
        mLon = lon;
        mName = name;
        mSurname = surname;
        mMarkerOptions = new MarkerOptions()
            .title(name + " " + surname)
                .visible(visible)
            .position(new LatLng(lat, lon))
            .anchor(0.5f, 0.5f);
        if (bitmap != null) {
            mMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }

    public String addOnMap(final GoogleMap map) {
        mMarker = map.addMarker(mMarkerOptions);
        mMarkerId = mMarker.getId();
        return mMarkerId;
    }

    public Integer getMarkerId() {
        return Integer.parseInt(mMarkerId.replace("m", ""));
    }

    public boolean getVisible() {
        if (mMarker != null) {
            return mMarker.isVisible();
        }
        return false;
    }

    public String getName() {
        return mName;
    }

    public String getSurname() {
        return mSurname;
    }

    public long getUserId() {
        return mUserId;
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    public void setLatLon(final double lat, final double lon) {
        mLat = lat;
        mLon = lon;
        mMarker.setPosition(new LatLng(lat, lon));
    }

    public void setVisible(final boolean visible) {
        mMarker.setVisible(visible);
    }
}
