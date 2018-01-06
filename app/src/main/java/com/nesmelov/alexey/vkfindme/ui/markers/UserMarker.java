package com.nesmelov.alexey.vkfindme.ui.markers;

import android.graphics.Bitmap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nesmelov.alexey.vkfindme.storage.Storage;

/**
 * User map marker.
 */
public class UserMarker {

    private Integer mVkId;
    private String mName = "";
    private String mSurname = "";
    private String mIconUrl = "bad url";
    private double mLat = Storage.BAD_LAT;
    private double mLon = Storage.BAD_LON;
    private boolean mVisible = false;

    private Marker mMarker;

    /**
     * Defauld constructor.
     */
    public UserMarker() { }

    /**
     * Constructs user marker.
     *
     * @param vkId VK id.
     * @param name user name.
     * @param surname user surname.
     * @param lat user latitude.
     * @param lon user longitude.
     */
    public UserMarker(final Integer vkId, final String name, final String surname,
                      final double lat, final double lon) {
        mVkId = vkId;
        mName = name;
        mSurname = surname;
        mLat = lat;
        mLon = lon;
    }

    /**
     * Sets marker icon.
     *
     * @param bitmap icon to set.
     */
    public void setIcon(final Bitmap bitmap) {
        if (mMarker != null) {
            mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }

    /**
     * Add user marker to map.
     *
     * @param map google map to add marker.
     */
    public void addToMap(final GoogleMap map) {
        final MarkerOptions markerOptions = new MarkerOptions()
                .title(mName + " " + mSurname)
                .visible(mVisible)
                .position(new LatLng(mLat, mLon))
                .anchor(0.5f, 0.5f);
        mMarker = map.addMarker(markerOptions);
    }

    /**
     * Gets map marker id.
     *
     * @return map marker id.
     */
    public String getMarkerId() {
        return mMarker != null ? mMarker.getId() : null;
    }

    /**
     * Returns <tt>true</tt> if user marker is visible.
     *
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
        mLat = lat;
        mLon = lon;
        if (mMarker != null) {
            mMarker.setPosition(new LatLng(lat, lon));
        }
    }

    /**
     * Sets visibility.
     *
     * @param visible visibility to set.
     */
    public void setVisible(final boolean visible) {
        mVisible = visible;
        if (mMarker != null) {
            mMarker.setVisible(visible);
        }
    }

    /**
     * Returns google marker.
     *
     * @return google marker.
     */
    public Marker getMarker() {
        return mMarker;
    }

    /**
     * Sets VK id.
     *
     * @param vkId VK id to set.
     */
    public void setVkId(final Integer vkId) {
        mVkId = vkId;
    }

    /**
     * Sets user name.
     *
     * @param name user name to set.
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * Sets user surname.
     *
     * @param surname user surname to set.
     */
    public void setSurname(final String surname) {
        mSurname = surname;
    }

    /**
     * Sets user icon url.
     *
     * @param iconUrl user icon url to set.
     */
    public void setIconUrl(final String iconUrl) {
        mIconUrl = iconUrl;
    }

    /**
     * Sets user latitude.
     *
     * @param lat latitude to set.
     */
    public void setLat(final double lat) {
        mLat = lat;
    }

    /**
     * Sets user longitude.
     *
     * @param lon longitude to set.
     */
    public void setLon(final double lon) {
        mLon = lon;
    }

    /**
     * Gets user vk id.
     *
     * @return user vk id.
     */
    public Integer getVkId() {
        return mVkId;
    }

    /**
     * Gets user name.
     *
     * @return user name.
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets user surname.
     *
     * @return user surname.
     */
    public String getSurname() {
        return mSurname;
    }

    /**
     * Gets user icon url.
     *
     * @return user icon url.
     */
    public String getIconUrl() {
        return mIconUrl;
    }

    /**
     * Gets user latitude.
     *
     * @return user latitude.
     */
    public double getLat() {
        return mLat;
    }

    /**
     * Gets user longitude.
     *
     * @return user longitude.
     */
    public double getLon() {
        return mLon;
    }

    /**
     * Returns <tt>true</tt> if user is visible.
     *
     * @return <tt>true</tt> if user is visible.
     */
    public boolean isVisible() {
        return mVisible;
    }
}
