package com.nesmelov.alexey.vkfindme.ui.marker;

import android.graphics.Bitmap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nesmelov.alexey.vkfindme.structures.User;

public class UserMarker extends User {
    private String mMarkerId;
    private Marker mMarker;
    final MarkerOptions mMarkerOptions;

    public UserMarker(final Integer vkId, final String name, final String surname,
                      final double lat, final double lon, final boolean visible, final Bitmap bitmap) {
        super(vkId, name, surname, lat, lon);
        mMarkerOptions = new MarkerOptions()
            .title(name + " " + surname)
                .visible(visible)
            .position(new LatLng(lat, lon))
            .anchor(0.5f, 0.5f);
        if (bitmap != null) {
            mMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }

    public String addToMap(final GoogleMap map) {
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

    public void setLatLon(final double lat, final double lon) {
        setLat(lat);
        setLon(lon);
        mMarker.setPosition(new LatLng(lat, lon));
    }

    public void setVisible(final boolean visible) {
        mMarker.setVisible(visible);
    }

    public Marker getMarker() {
        return mMarker;
    }
}
