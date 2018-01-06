package com.nesmelov.alexey.vkfindme.ui.markers;

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
import com.nesmelov.alexey.vkfindme.utils.Utils;

import java.util.ArrayList;

/**
 * Alarm marker.
 */
public class AlarmMarker {
    private static final int ALARM_SIZE_DP = 40;

    private int mAlarmId;
    private double mLat;
    private double mLon;
    private float mRadius;
    private int mColor;

    private ArrayList<Integer> mUsers = new ArrayList<>();
    private String mNames = "";

    private Marker mMarker;
    private Circle mCircle;

    /**
     * Constructs alarm marker instance.
     *
     * @param alarmId alarm id.
     * @param lat alarm latitude.
     * @param lon alarm longitude.
     * @param radius alarm radius.
     * @param color alarm color.
     */
    public AlarmMarker(final int alarmId, final double lat, final double lon, final float radius, final int color) {
        mAlarmId = alarmId;
        mLat = lat;
        mLon = lon;
        mRadius = radius;
        mColor = color;
    }

    /**
     * Constructs alarm marker instance.
     *
     * @param alarmId alarm id.
     * @param lat alarm latitude.
     * @param lon alarm longitude.
     * @param radius alarm radius.
     * @param color alarm color.
     * @param users alarm users.
     * @param names alarm users names.
     */
    public AlarmMarker(final int alarmId, final double lat, final double lon, final float radius, final int color,
                 final ArrayList<Integer> users, final String names) {
        this(alarmId, lat, lon, radius, color);
        mUsers = users;
        mNames = names;
    }

    /**
     * Adds alarm marker to the map.
     *
     * @param context context to use.
     * @param map map to add marker.
     */
    public void addToMap(final Context context, final GoogleMap map) {
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
    }

    /**
     * Gets alarm circle.
     *
     * @return alarm circle.
     */
    public Circle getCircle() {
        return mCircle;
    }

    /**
     * Gets google marker.
     *
     * @return google marker.
     */
    public Marker getMarker() {
        return mMarker;
    }

    /**
     * Gets alarm id.
     *
     * @return alarm id.
     */
    public Integer getAlarmId() {
        return mAlarmId;
    }

    /**
     * Gets alarm latitude.
     *
     * @return alarm latitude.
     */
    public double getLat() {
        return mLat;
    }

    /**
     * Gets alarm longitude.
     *
     * @return alarm longitude.
     */
    public double getLon() {
        return mLon;
    }

    /**
     * Gets alarm radius.
     *
     * @return alarm radius.
     */
    public float getRadius() {
        return mRadius;
    }

    /**
     * Gets alarm users.
     *
     * @return alarm users.
     */
    public ArrayList<Integer> getUsers() {
        return mUsers;
    }

    /**
     * Gets alarm user names.
     *
     * @return alarm user names.
     */
    public String getNames() {
        return mNames;
    }

    /**
     * Sets alarm users.
     *
     * @param users users to set.
     */
    public void setUsers(final ArrayList<Integer> users) {
        mUsers = users;
    }

    /**
     * Sets alarm users names.
     *
     * @param names alarm users names to set.
     */
    public void setNames(final String names) {
        mNames = names;
    }

    /**
     * Gets alarm color.
     *
     * @return alarm color.
     */
    public int getColor() {
        return mColor;
    }
}
