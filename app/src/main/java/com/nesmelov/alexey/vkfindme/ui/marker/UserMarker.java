package com.nesmelov.alexey.vkfindme.ui.marker;

import android.graphics.Bitmap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nesmelov.alexey.vkfindme.structures.User;

/**
 * User map marker.
 */
public class UserMarker extends User {

    private String mMarkerId;
    private Marker mMarker;
    private final MarkerOptions mMarkerOptions;

    /**
     * Constructs user marker.
     *
     * @param vkId user id.
     * @param name user name.
     * @param surname user surname.
     * @param lat user latitude.
     * @param lon user longitude.
     * @param visible user visibility.
     * @param bitmap user icon.
     */
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

    /**
     * Add user marker to map.
     *
     * @param map google map to add marker.
     * @return marker id.
     */
    public String addToMap(final GoogleMap map) {
        mMarker = map.addMarker(mMarkerOptions);
        mMarkerId = mMarker.getId();
        return mMarkerId;
    }

    /**
     * Returns marker id.
     *
     * @return marker id.
     */
    public Integer getMarkerId() {
        return Integer.parseInt(mMarkerId.replace("m", ""));
    }

    /**
     * Returns <tt>true</tt> if user marker is visible.
     * @return <tt>true</tt> if user marker is visible.
     */
    public boolean getVisible() {
        return mMarker != null && mMarker.isVisible();
    }

    /**
     * Sets marker lat lon.
     *
     * @param lat latitude.
     * @param lon longitude.
     */
    public void setLatLon(final double lat, final double lon) {
        setLat(lat);
        setLon(lon);
        mMarker.setPosition(new LatLng(lat, lon));
    }

    /**
     * Sets visibility.
     *
     * @param visible visibility to set.
     */
    public void setVisible(final boolean visible) {
        mMarker.setVisible(visible);
    }

    /**
     * Returns google marker.
     *
     * @return google marker.
     */
    public Marker getMarker() {
        return mMarker;
    }
}
