package com.nesmelov.alexey.vkfindme.storage;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.structures.User;
import com.nesmelov.alexey.vkfindme.ui.AlarmMarker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Storage {
    public static final float MIN_ALARM_RADIUS = 100;

    public static final String USER_VK_ID = "user_vk_id";
    public static final String USER_NAME = "user_name";
    public static final String USER_SURNAME = "user_surname";
    public static final String USER_ICON_URL = "user_icon_url";
    public static final String VISIBILITY = "visibility";
    public static final String GPS_MIN_DELAY = "gps_min_delay";
    public static final String GPS_MIN_DISTANCE = "gps_min_distance";
    public static final String ALARM_RADIUS = "alarm_radius";

    private SharedPreferences mSharedPrefs;
    private DataBaseHelper mDataBaseHelper;

    private List<OnAlarmRemovedListener> mAlarmRemovedListeners = new CopyOnWriteArrayList<>();

    public Storage(final Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mDataBaseHelper = FindMeApp.getDataBaseHelper();
    }

    public void addAlarmRemovedListener(final OnAlarmRemovedListener listener) {
        mAlarmRemovedListeners.add(listener);
    }

    public void removeAlarmRemovedListener(final OnAlarmRemovedListener listener) {
        mAlarmRemovedListeners.remove(listener);
    }

    public String getUserName() {
        return mSharedPrefs.getString(USER_NAME, "Алексей");
    }

    public String getUserSurname() {
        return mSharedPrefs.getString(USER_SURNAME, "Евгеньевич");
    }

    public String getUserIconUrl() {
        return mSharedPrefs.getString(USER_ICON_URL, "");
    }

    public Integer getUserVkId() {
        return mSharedPrefs.getInt(USER_VK_ID, 0);
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

    public Float getAlarmRadius() {
        return  mSharedPrefs.getFloat(ALARM_RADIUS, MIN_ALARM_RADIUS);
    }

    public void setUserVkId(final Integer userVkId) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putInt(USER_VK_ID, userVkId);
        ed.commit();
    }

    public void setUserName(final String userName) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(USER_NAME, userName);
        ed.commit();
    }

    public void setUserSurname(final String userSurname) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(USER_SURNAME, userSurname);
        ed.commit();
    }

    public void setUserIconUrl(final String userIconUrl) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(USER_ICON_URL, userIconUrl);
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

    public void setAlarmRadius(final Float radius) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(ALARM_RADIUS, radius);
        ed.commit();
    }

    public long addUser(final long vkId, final String name, final String surname,
                        final double lat, final double lon, final String photoUrl) {
        final ContentValues userValues = new ContentValues();
        userValues.put(DataBaseHelper.VK_ID, vkId);
        userValues.put(DataBaseHelper.NAME, name);
        userValues.put(DataBaseHelper.SURNAME, surname);
        userValues.put(DataBaseHelper.LATITUDE, lat);
        userValues.put(DataBaseHelper.LONGITUDE, lon);
        userValues.put(DataBaseHelper.PHOTO_URL, photoUrl);
        userValues.put(DataBaseHelper.VISIBLE, 0);
        return mDataBaseHelper.insertUser(userValues);
    }

    public List<Alarm> getAlarmsForMe() {
        return mDataBaseHelper.getAlarmsForMe();
    }

    public long addAlarm(final double lat, final double lon, final float radius, final List<Integer> users) {
        final ContentValues alarmValues = new ContentValues();
        alarmValues.put(DataBaseHelper.LATITUDE, lat);
        alarmValues.put(DataBaseHelper.LONGITUDE, lon);
        alarmValues.put(DataBaseHelper.RADIUS, radius);
        alarmValues.put(DataBaseHelper.CHECKED, 1);
        final long alarmId = mDataBaseHelper.insertAlarm(alarmValues);

        for (final Integer user : users) {
            final ContentValues alarmUsersValues = new ContentValues();
            alarmUsersValues.put(DataBaseHelper.ALARM_ID, alarmId);
            alarmUsersValues.put(DataBaseHelper.USER_ID, user);
            mDataBaseHelper.insertAlarmUsers(alarmUsersValues);
        }

        return alarmId;
    }

    public long updateAlarm(final long alarmId, final List<Integer> users) {
        mDataBaseHelper.removeAlarmUsers(alarmId);

        for (final Integer user : users) {
            final ContentValues alarmUsersValues = new ContentValues();
            alarmUsersValues.put(DataBaseHelper.ALARM_ID, alarmId);
            alarmUsersValues.put(DataBaseHelper.USER_ID, user);
            mDataBaseHelper.insertAlarmUsers(alarmUsersValues);
        }

        return alarmId;
    }

    public void removeAlarm(final long alarmId) {
        mDataBaseHelper.removeAlarm(alarmId);
        for (final OnAlarmRemovedListener listener : mAlarmRemovedListeners) {
            listener.onRemoved(alarmId);
        }
    }

    public boolean isMeInAlarm() {
        return mDataBaseHelper.isMeInAlarm();
    }

    public List<User> getFriends(final long limit) {
        return mDataBaseHelper.getUsers(limit);
    }

    public List<AlarmMarker> getAlarmMarkers(final Context context, final GoogleMap map) {
        return mDataBaseHelper.getAlarmMarkers(context, map);
    }
}
