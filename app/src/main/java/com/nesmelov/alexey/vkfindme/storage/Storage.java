package com.nesmelov.alexey.vkfindme.storage;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.structures.User;
import com.nesmelov.alexey.vkfindme.ui.AlarmMarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    public static final String USER_LAT = "user_lat";
    public static final String USER_LON = "user_lon";
    public static final String REFRESH_FRIENDS_DELAY = "refresh_friends_delay";
    public static final String REFRESH_FRIENDS = "refresh_friends";

    private SharedPreferences mSharedPrefs;
    private DataBaseHelper mDataBaseHelper;

    private List<OnAlarmUpdatedListener> mAlarmUpdatedListeners = new CopyOnWriteArrayList<>();
    private List<OnUserUpdatedListener> mUserUpdatedListeners = new CopyOnWriteArrayList<>();

    public Storage(final Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mDataBaseHelper = FindMeApp.getDataBaseHelper();
    }

    public void addAlarmUpdatedListener(final OnAlarmUpdatedListener listener) {
        mAlarmUpdatedListeners.add(listener);
    }

    public String getUserIdsString() {
        return mDataBaseHelper.getUserIdsString(Const.FRIENDS_LIMIT);
    }

    public List<Integer> getUserIds() {
        return mDataBaseHelper.getUserIds(Const.FRIENDS_LIMIT);
    }

    public void removeAlarmUpdatedListener(final OnAlarmUpdatedListener listener) {
        mAlarmUpdatedListeners.remove(listener);
    }

    public void addUserUpdatedListener(final OnUserUpdatedListener listener) {
        mUserUpdatedListeners.add(listener);
    }

    public void removeUserUpdatedListener(final OnUserUpdatedListener listener) {
        mUserUpdatedListeners.remove(listener);
    }

    public boolean getRefreshFriends() {
        return mSharedPrefs.getBoolean(REFRESH_FRIENDS, false);
    }

    public double getUserLat() {
        return mSharedPrefs.getFloat(USER_LAT, 0);
    }

    public double getUserLon() {
        return mSharedPrefs.getFloat(USER_LON, 0);
    }

    public String getUserName() {
        return mSharedPrefs.getString(USER_NAME, "");
    }

    public String getUserSurname() {
        return mSharedPrefs.getString(USER_SURNAME, "");
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
        return mSharedPrefs.getFloat(ALARM_RADIUS, MIN_ALARM_RADIUS);
    }

    public Long getRefreshFriendsDelay() {
        return mSharedPrefs.getLong(REFRESH_FRIENDS_DELAY, 3000);
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

    public void setRefreshFriendsDelay(final long refreshFriendsDelay) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putLong(REFRESH_FRIENDS_DELAY, refreshFriendsDelay);
        ed.commit();
    }

    public void setRefreshFriends(final boolean refreshFriends) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putBoolean(REFRESH_FRIENDS, refreshFriends);
        ed.commit();
    }

    public void setUserLat(final double lat) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(USER_LAT, (float)lat);
        ed.commit();
    }

    public void setUserLon(final double lon) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(USER_LON, (float)lon);
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

    public void setUserPos(final Integer userId, final double lat, final double lon) {
        final ContentValues values = new ContentValues();
        values.put(DataBaseHelper.VK_ID, userId);
        values.put(DataBaseHelper.LATITUDE, lat);
        values.put(DataBaseHelper.LONGITUDE, lon);
        values.put(DataBaseHelper.VISIBLE, 1);
        mDataBaseHelper.updateUser(userId, values);
        for (final OnUserUpdatedListener listener : mUserUpdatedListeners) {
            listener.onUserUpdated(userId, lat, lon);
        }
    }

    public void makeUserInvisible(final Integer userId) {
        final ContentValues values = new ContentValues();
        values.put(DataBaseHelper.VK_ID, userId);
        values.put(DataBaseHelper.VISIBLE, 0);
        mDataBaseHelper.updateUser(userId, values);
        for (final OnUserUpdatedListener listener : mUserUpdatedListeners) {
            listener.onUserInvisible(userId);
        }
    }

    public long addUser(final User user) {
        final ContentValues userValues = new ContentValues();
        userValues.put(DataBaseHelper.VK_ID, user.getVkId());
        userValues.put(DataBaseHelper.NAME, user.getName());
        userValues.put(DataBaseHelper.SURNAME, user.getSurname());
        userValues.put(DataBaseHelper.LATITUDE, Const.BAD_LAT);
        userValues.put(DataBaseHelper.LONGITUDE, Const.BAD_LON);
        userValues.put(DataBaseHelper.PHOTO_URL, user.getIconUrl());
        userValues.put(DataBaseHelper.VISIBLE, 0);
        return mDataBaseHelper.insertUser(userValues);
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

    public List<User> getFriends() {
        return mDataBaseHelper.getUsers(Const.FRIENDS_LIMIT);
    }

    public List<Alarm> getAlarmsForMe() {
        return mDataBaseHelper.getAlarmsForMe();
    }

    public boolean isAlarmCompleted(final long alarmId) {
        return mDataBaseHelper.isAlarmCompleted(alarmId);
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
        for (final OnAlarmUpdatedListener listener : mAlarmUpdatedListeners) {
            listener.onAlarmRemoved(alarmId);
        }
    }

    public void removeAlarmParticipant(final long alarmId, final long userId) {
        mDataBaseHelper.removeAlarmUser(alarmId, userId);
        for (final OnAlarmUpdatedListener listener : mAlarmUpdatedListeners) {
            listener.onAlarmUpdated(alarmId);
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

    public AlarmMarker getAlarmMarker(final long alarmId) {
        return mDataBaseHelper.getAlarmMarker(alarmId);
    }

    public Map<User, List<Alarm>> getAlarmUsers() {
        return mDataBaseHelper.getAlarmUsers();
    }
}
