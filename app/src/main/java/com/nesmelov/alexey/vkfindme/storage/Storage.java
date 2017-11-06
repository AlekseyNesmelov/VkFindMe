package com.nesmelov.alexey.vkfindme.storage;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.android.gms.maps.GoogleMap;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.network.models.UserModel;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.structures.User;
import com.nesmelov.alexey.vkfindme.ui.marker.AlarmMarker;
import java.util.List;
import java.util.Map;

import static com.nesmelov.alexey.vkfindme.application.FindMeApp.USERS_DATABASE_NAME;

public class Storage {
    public static final String USER = "user";
    public static final String USERS = "users";
    public static final String VISIBLE = "visible";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String RADIUS = "radius";
    public static final String ALARM_ID = "alarm_id";
    public static final String NAMES = "names";
    public static final String COLOR = "color";

    public static final double BAD_LAT = -999;
    public static final double BAD_LON = -999;
    public static final long BAD_ID = -1;
    public static final int BAD_USER_ID = 0;
    public static final float BAD_RADIUS = 0;

    public static final int FRIENDS_LIMIT = 100;

    public static final int INVISIBLE_STATE = 0;
    public static final int VISIBLE_STATE = 1;

    public static final int RESULT_UPDATE = 2;

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

    private OnAlarmUpdatedListener mAlarmUpdatedListener = null;
    private OnUserUpdatedListener mUserUpdatedListener = null;

    private final Object mLock = new Object();
    private Context mContext;

    public Storage(final Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mDataBaseHelper = new DataBaseHelper(context, USERS_DATABASE_NAME, null);
        mContext = context;
    }

    public void setAlarmUpdatedListener(final OnAlarmUpdatedListener listener) {
        synchronized (mLock) {
            mAlarmUpdatedListener = listener;
        }
    }

    public boolean isAlarmUpdateListenerExist() {
        synchronized (mLock) {
            return mAlarmUpdatedListener != null;
        }
    }

    public List<UserModel> getUserModels() {
        return mDataBaseHelper.getUserModels(FRIENDS_LIMIT);
    }

    public List<Integer> getUserIds() {
        return mDataBaseHelper.getUserIds(FRIENDS_LIMIT);
    }

    public void removeAlarmUpdatedListener() {
        synchronized (mLock) {
            mAlarmUpdatedListener = null;
        }
    }

    public void setUserUpdatedListener(final OnUserUpdatedListener listener) {
        synchronized (mLock) {
            mUserUpdatedListener = listener;
        }
    }

    public void removeUserUpdatedListener() {
        synchronized (mLock) {
            mUserUpdatedListener = null;
        }
    }

    public boolean isUserUpdateListenerExist() {
        synchronized (mLock) {
            return mUserUpdatedListener != null;
        }
    }

    public boolean isAlarmExist() {
        return mDataBaseHelper.isAlarmExist();
    }

    public boolean getRefreshFriends() {
        return mSharedPrefs.getBoolean(REFRESH_FRIENDS, true);
    }

    public double getUserLat() {
        return mSharedPrefs.getFloat(USER_LAT, (float) BAD_LAT);
    }

    public double getUserLon() {
        return mSharedPrefs.getFloat(USER_LON, (float)BAD_LON);
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
        return mSharedPrefs.getInt(USER_VK_ID, BAD_USER_ID);
    }

    public Boolean getVisibility() {
        return mSharedPrefs.getBoolean(VISIBILITY, false);
    }

    public Long getGPSMinDelay() {
        return Long.parseLong(mSharedPrefs.getString(GPS_MIN_DELAY,
                mContext.getString(R.string.send_pos_default_delay)));
    }

    public Float getGPSMinDistance() {
        return Float.parseFloat(mSharedPrefs.getString(GPS_MIN_DISTANCE,
                mContext.getString(R.string.min_gps_distance_default)));
    }

    public Float getAlarmRadius() {
        return mSharedPrefs.getFloat(ALARM_RADIUS, MIN_ALARM_RADIUS);
    }

    public Long getRefreshFriendsDelay() {
        return Long.parseLong(mSharedPrefs.getString(REFRESH_FRIENDS_DELAY,
                mContext.getString(R.string.friend_refresh_default_delay)));
    }

    public void setUserVkId(final Integer userVkId) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putInt(USER_VK_ID, userVkId);
        ed.apply();
        mDataBaseHelper.recreateTables();
    }

    public void setUserName(final String userName) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(USER_NAME, userName);
        ed.apply();
    }

    public void setRefreshFriendsDelay(final long refreshFriendsDelay) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(REFRESH_FRIENDS_DELAY, String.valueOf(refreshFriendsDelay));
        ed.apply();
    }

    public void setRefreshFriends(final boolean refreshFriends) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putBoolean(REFRESH_FRIENDS, refreshFriends);
        ed.apply();
    }

    public void setUserLat(final double lat) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(USER_LAT, (float)lat);
        ed.apply();
    }

    public void setUserLon(final double lon) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(USER_LON, (float)lon);
        ed.apply();
    }

    public void setUserSurname(final String userSurname) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(USER_SURNAME, userSurname);
        ed.apply();
    }

    public void setUserIconUrl(final String userIconUrl) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(USER_ICON_URL, userIconUrl);
        ed.apply();
    }

    public void setVisibility(final Boolean visibility) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putBoolean(VISIBILITY, visibility);
        ed.apply();
    }

    public void setGPSMinDelay(final Long delay) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(GPS_MIN_DELAY, String.valueOf(delay));
        ed.apply();
    }

    public void setGPSMinDistance(final Float distance) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(GPS_MIN_DISTANCE, distance);
        ed.apply();
    }

    public void setAlarmRadius(final Float radius) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(ALARM_RADIUS, radius);
        ed.apply();
    }

    public void setUserPos(final Integer userId, final double lat, final double lon) {
        final ContentValues values = new ContentValues();
        values.put(DataBaseHelper.VK_ID, userId);
        values.put(DataBaseHelper.LATITUDE, lat);
        values.put(DataBaseHelper.LONGITUDE, lon);
        values.put(DataBaseHelper.VISIBLE, 1);
        mDataBaseHelper.updateUser(userId, values);
        synchronized (mLock) {
            if (mUserUpdatedListener != null) {
                mUserUpdatedListener.onUserUpdated(userId, lat, lon);
            }
        }
    }

    public void makeUserInvisible(final Integer userId) {
        final ContentValues values = new ContentValues();
        values.put(DataBaseHelper.VK_ID, userId);
        values.put(DataBaseHelper.VISIBLE, 0);
        mDataBaseHelper.updateUser(userId, values);
        synchronized (mLock) {
            if (mUserUpdatedListener != null) {
                mUserUpdatedListener.onUserInvisible(userId);
            }
        }
    }

    public long addUser(final User user) {
        final ContentValues userValues = new ContentValues();
        userValues.put(DataBaseHelper.VK_ID, user.getVkId());
        userValues.put(DataBaseHelper.NAME, user.getName());
        userValues.put(DataBaseHelper.SURNAME, user.getSurname());
        userValues.put(DataBaseHelper.LATITUDE, BAD_LAT);
        userValues.put(DataBaseHelper.LONGITUDE, BAD_LON);
        userValues.put(DataBaseHelper.PHOTO_URL, user.getIconUrl());
        userValues.put(DataBaseHelper.VISIBLE, INVISIBLE_STATE);
        return mDataBaseHelper.insertUser(userValues);
    }

    public List<User> getFriends() {
        return mDataBaseHelper.getUsers(FRIENDS_LIMIT);
    }

    public List<Alarm> getAlarmsForMe() {
        return mDataBaseHelper.getAlarmsForMe();
    }

    public boolean isAlarmCompleted(final long alarmId) {
        return mDataBaseHelper.isAlarmCompleted(alarmId);
    }

    public long addAlarm(final double lat, final double lon, final float radius, final int color,
                         final List<Integer> users) {
        final ContentValues alarmValues = new ContentValues();
        alarmValues.put(DataBaseHelper.LATITUDE, lat);
        alarmValues.put(DataBaseHelper.LONGITUDE, lon);
        alarmValues.put(DataBaseHelper.RADIUS, radius);
        alarmValues.put(DataBaseHelper.CHECKED, 1);
        alarmValues.put(DataBaseHelper.COLOR, color);
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
        synchronized (mLock) {
            if (mAlarmUpdatedListener != null) {
                mAlarmUpdatedListener.onAlarmRemoved(alarmId);
            }
        }
    }

    public void removeAlarmParticipant(final long alarmId, final long userId) {
        mDataBaseHelper.removeAlarmUser(alarmId, userId);
        synchronized (mLock) {
            if (mAlarmUpdatedListener != null) {
                mAlarmUpdatedListener.onAlarmUpdated(alarmId);
            }
        }
    }

    public boolean isMeInAlarm() {
        return mDataBaseHelper.isMeInAlarm();
    }

    public List<User> getFriends(final long limit) {
        return mDataBaseHelper.getUsers(limit);
    }

    public Map<Long, AlarmMarker> getAlarmMarkers() {
        return mDataBaseHelper.getAlarmMarkers();
    }

    public AlarmMarker getAlarmMarker(final long alarmId) {
        return mDataBaseHelper.getAlarmMarker(alarmId);
    }

    public Map<Integer, List<Alarm>> getAlarmUsers() {
        return mDataBaseHelper.getAlarmUsers();
    }

    public String getUserName(final int id) {
        return mDataBaseHelper.getUserName(id);
    }
}
