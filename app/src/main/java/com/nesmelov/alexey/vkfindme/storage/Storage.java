package com.nesmelov.alexey.vkfindme.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Storage {
    public static final String USER = "user";
    public static final String VISIBILITY = "visibility";
    public static final String GPS_MIN_DELAY = "gps_min_delay";
    public static final String GPS_MIN_DISTANCE = "gps_min_distance";

    public static final String USERS_DATABASE_NAME = "USERS_DATABASE";

    private SharedPreferences mSharedPrefs;
    private DataBaseHelper mDataBaseHelper;

    public Storage(final Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mDataBaseHelper = new DataBaseHelper(context, USERS_DATABASE_NAME, null);
    }

    public Integer getUser() {
        return mSharedPrefs.getInt(USER, 0);
    }

    public Boolean getVisibility() {
        return mSharedPrefs.getBoolean(VISIBILITY, false);
    }

    public Long getGPSMinDelay() {
        return mSharedPrefs.getLong(GPS_MIN_DELAY, 3000);
    }

    public Float getGPSMinDistance() {
        return mSharedPrefs.getFloat(GPS_MIN_DISTANCE, 2);
    }

    public void setUser(final String user) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(USER, user);
        ed.commit();
    }

    public void setVisibility(final Boolean visibility) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putBoolean(VISIBILITY, visibility);
        ed.commit();
    }

    public void setGPSMinDelay(final Long delay) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putLong(GPS_MIN_DELAY, delay);
        ed.commit();
    }

    public void setGPSMinDistance(final Float distance) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(GPS_MIN_DISTANCE, distance);
        ed.commit();
    }
}
