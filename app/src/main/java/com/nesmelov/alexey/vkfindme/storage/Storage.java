package com.nesmelov.alexey.vkfindme.storage;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.ui.markers.AlarmMarker;
import com.nesmelov.alexey.vkfindme.ui.markers.UserMarker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Find me application storage.
 */
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
    public static final int BAD_ID = -1;
    public static final int BAD_USER_ID = 0;
    public static final float BAD_RADIUS = 0;

    public static final int FRIENDS_LIMIT = 3000;

    public static final int INVISIBLE_STATE = 0;
    public static final int VISIBLE_STATE = 1;

    public static final int RESULT_REMOVE = 2;

    public static final float MIN_ALARM_RADIUS = 100;

    private static final String USER_VK_ID = "user_vk_id";
    private static final String USER_NAME = "user_name";
    private static final String USER_SURNAME = "user_surname";
    private static final String USER_ICON_URL = "user_icon_url";
    private static final String VISIBILITY = "visibility";
    private static final String GPS_MIN_DELAY = "gps_min_delay";
    private static final String GPS_MIN_DISTANCE = "gps_min_distance";
    private static final String ALARM_RADIUS = "alarm_radius";
    private static final String USER_LAT = "user_lat";
    private static final String USER_LON = "user_lon";
    private static final String REFRESH_FRIENDS_DELAY = "refresh_friends_delay";
    private static final String REFRESH_FRIENDS = "refresh_friends";
    private static final String USERS_DATABASE_NAME = "USERS_DATABASE";

    private OnAlarmUpdatedListener mAlarmUpdatedListener = null;
    private OnUserUpdatedListener mUserUpdatedListener = null;

    private final Object mLock = new Object();
    private final Context mContext;
    private final SharedPreferences mSharedPrefs;
    private final DataBaseHelper mDataBaseHelper;

    /**
     * Constructs storage object.
     *
     * @param context context to use.
     */
    public Storage(final Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mDataBaseHelper = new DataBaseHelper(context, USERS_DATABASE_NAME, null);
        mContext = context;
    }

    /**
     * Sets alarm updated listener.
     *
     * @param listener listener to set.
     */
    public void setAlarmUpdatedListener(final OnAlarmUpdatedListener listener) {
        synchronized (mLock) {
            mAlarmUpdatedListener = listener;
        }
    }

    /**
     * Returns <tt>true</tt> if alarm updated listener exists.
     *
     * @return <tt>true</tt> if alarm updated listener exists.
     */
    public boolean isAlarmUpdateListenerExist() {
        synchronized (mLock) {
            return mAlarmUpdatedListener != null;
        }
    }

    /**
     * Returns list of users ids.
     *
     * @return list of users ids.
     */
    public List<Integer> getUserIds() {
        return mDataBaseHelper.getUserIds(FRIENDS_LIMIT);
    }

    /**
     * Removes alarm updated listener.
     */
    public void removeAlarmUpdatedListener() {
        synchronized (mLock) {
            mAlarmUpdatedListener = null;
        }
    }

    /**
     * Sets user updated listener.
     *
     * @param listener listener to set.
     */
    public void setUserUpdatedListener(final OnUserUpdatedListener listener) {
        synchronized (mLock) {
            mUserUpdatedListener = listener;
        }
    }

    /**
     * Removes user updated listener.
     */
    public void removeUserUpdatedListener() {
        synchronized (mLock) {
            mUserUpdatedListener = null;
        }
    }

    /**
     * Returns <tt>true</tt> if user updated listener.
     *
     * @return <tt>true</tt> if user updated listener.
     */
    public boolean isUserUpdatedListenerExist() {
        synchronized (mLock) {
            return mUserUpdatedListener != null;
        }
    }

    /**
     * Returns <tt>true</tt> friends alarm exists.
     *
     * @return <tt>true</tt> friends alarm exists.
     */
    public boolean isAlarmForFriendExist() {
        return mDataBaseHelper.isAlarmForFriendExist();
    }

    /**
     * Returns <tt>true</tt> if it is needed to refresh friends.
     *
     * @return <tt>true</tt> if it is needed to refresh friends.
     */
    public boolean getRefreshFriends() {
        return mSharedPrefs.getBoolean(REFRESH_FRIENDS, true);
    }

    /**
     * Gets current user latitude.
     *
     * @return current user latitude.
     */
    public double getUserLat() {
        return mSharedPrefs.getFloat(USER_LAT, (float) BAD_LAT);
    }

    /**
     * Gets current user longitude.
     *
     * @return current user longitude.
     */
    public double getUserLon() {
        return mSharedPrefs.getFloat(USER_LON, (float)BAD_LON);
    }

    /**
     * Gets current user name.
     *
     * @return current user name.
     */
    public String getUserName() {
        return mSharedPrefs.getString(USER_NAME, "");
    }

    /**
     * Gets current user surname.
     *
     * @return current user surname.
     */
    public String getUserSurname() {
        return mSharedPrefs.getString(USER_SURNAME, "");
    }

    /**
     * Gets current user icon url.
     *
     * @return current user icon url.
     */
    public String getUserIconUrl() {
        return mSharedPrefs.getString(USER_ICON_URL, "bad url");
    }

    /**
     * Gets current user VK id.
     *
     * @return current user VK id.
     */
    public Integer getUserVkId() {
        return mSharedPrefs.getInt(USER_VK_ID, BAD_USER_ID);
    }

    /**
     * Gets current user visibility.
     *
     * @return current user visibility.
     */
    public Boolean getVisibility() {
        return mSharedPrefs.getBoolean(VISIBILITY, false);
    }

    /**
     * Gets GPS min delay property.
     *
     * @return GPS min delay property.
     */
    public Long getGPSMinDelay() {
        return Long.parseLong(mSharedPrefs.getString(GPS_MIN_DELAY,
                mContext.getString(R.string.send_pos_default_delay)));
    }

    /**
     * Gets GPS min distance property.
     *
     * @return GPS min distance property.
     */
    public Float getGPSMinDistance() {
        return Float.parseFloat(mSharedPrefs.getString(GPS_MIN_DISTANCE,
                mContext.getString(R.string.min_gps_distance_default)));
    }

    /**
     * Gets current alarm radius.
     *
     * @return current alarm radius.
     */
    public Float getAlarmRadius() {
        return mSharedPrefs.getFloat(ALARM_RADIUS, MIN_ALARM_RADIUS);
    }

    /**
     * Gets refresh friends delay property.
     *
     * @return refresh friends delay property.
     */
    public Long getRefreshFriendsDelay() {
        return Long.parseLong(mSharedPrefs.getString(REFRESH_FRIENDS_DELAY,
                mContext.getString(R.string.friend_refresh_default_delay)));
    }

    /**
     * Sets current user VK id.
     *
     * @param userVkId vk id to set.
     */
    public void setUserVkId(final Integer userVkId) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putInt(USER_VK_ID, userVkId);
        ed.apply();
        mDataBaseHelper.recreateTables();
    }

    /**
     * Sets current user name.
     *
     * @param userName user name to set.
     */
    public void setUserName(final String userName) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(USER_NAME, userName);
        ed.apply();
    }

    /**
     * Sets refresh friends property.
     *
     * @param refreshFriends <tt>true</tt> if it needed to refresh friends.
     */
    public void setRefreshFriends(final boolean refreshFriends) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putBoolean(REFRESH_FRIENDS, refreshFriends);
        ed.apply();
    }

    /**
     * Sets current user latitude.
     *
     * @param lat latitude to set.
     */
    public void setUserLat(final double lat) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(USER_LAT, (float)lat);
        ed.apply();
    }

    /**
     * Sets current user longitude.
     *
     * @param lon longitude to set.
     */
    public void setUserLon(final double lon) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(USER_LON, (float)lon);
        ed.apply();
    }

    /**
     * Sets current user surname.
     *
     * @param userSurname user surname to set.
     */
    public void setUserSurname(final String userSurname) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(USER_SURNAME, userSurname);
        ed.apply();
    }

    /**
     * Sets current user icon url.
     *
     * @param userIconUrl user icon url to set.
     */
    public void setUserIconUrl(final String userIconUrl) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putString(USER_ICON_URL, userIconUrl);
        ed.apply();
    }

    /**
     * Sets current user visibility.
     *
     * @param visibility visibility to set.
     */
    public void setVisibility(final Boolean visibility) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putBoolean(VISIBILITY, visibility);
        ed.apply();
    }

    /**
     * Sets alarm radius.
     *
     * @param radius radius to set.
     */
    public void setAlarmRadius(final Float radius) {
        final SharedPreferences.Editor ed = mSharedPrefs.edit();
        ed.putFloat(ALARM_RADIUS, radius);
        ed.apply();
    }

    /**
     * Sets user position.
     *
     * @param userId user id to set position.
     * @param lat latitude to set.
     * @param lon longitude to set.
     */
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

    /**
     * Makes user invisible.
     *
     * @param userId user id to make invisible.
     */
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

    /**
     * Adds user to database.
     *
     * @param user user to add to database.
     */
    public void addUser(final UserMarker user) {
        final ContentValues userValues = new ContentValues();
        userValues.put(DataBaseHelper.VK_ID, user.getVkId());
        userValues.put(DataBaseHelper.NAME, user.getName());
        userValues.put(DataBaseHelper.SURNAME, user.getSurname());
        userValues.put(DataBaseHelper.LATITUDE, BAD_LAT);
        userValues.put(DataBaseHelper.LONGITUDE, BAD_LON);
        userValues.put(DataBaseHelper.PHOTO_URL, user.getIconUrl());
        userValues.put(DataBaseHelper.VISIBLE, INVISIBLE_STATE);
        mDataBaseHelper.insertUser(userValues);
    }

    /**
     * Gets cursor of friends.
     *
     * @return cursor of friends.
     */
    public Cursor getFriends() {
        return getFriends(FRIENDS_LIMIT);
    }

    /**
     * Gets alarms for current users.
     *
     * @return alarms for current users.
     */
    public List<AlarmMarker> getAlarmsForMe() {
        return mDataBaseHelper.getAlarmsForMe();
    }

    /**
     * Checks if the alarm hasn't participants.
     *
     * @param alarmId alarm id to check.
     * @return <tt>true</tt> if the alarm hasn't participants.
     */
    public boolean isAlarmCompleted(final int alarmId) {
        return mDataBaseHelper.isAlarmCompleted(alarmId);
    }

    /**
     * Adds a new alarm to database.
     *
     * @param lat alarm latitude.
     * @param lon alarm longitude.
     * @param radius alarm radius.
     * @param color alarm color.
     * @param users alarm users.
     * @return alarm id.
     */
    public int addAlarm(final double lat, final double lon, final float radius, final int color,
                         final List<Integer> users) {
        final ContentValues alarmValues = new ContentValues();
        alarmValues.put(DataBaseHelper.LATITUDE, lat);
        alarmValues.put(DataBaseHelper.LONGITUDE, lon);
        alarmValues.put(DataBaseHelper.RADIUS, radius);
        alarmValues.put(DataBaseHelper.CHECKED, 1);
        alarmValues.put(DataBaseHelper.COLOR, color);
        final int alarmId = mDataBaseHelper.insertAlarm(alarmValues);

        for (final Integer user : users) {
            final ContentValues alarmUsersValues = new ContentValues();
            alarmUsersValues.put(DataBaseHelper.ALARM_ID, alarmId);
            alarmUsersValues.put(DataBaseHelper.USER_ID, user);
            mDataBaseHelper.insertAlarmUsers(alarmUsersValues);
        }

        return alarmId;
    }

    /**
     * Updates alarm in the database.
     *
     * @param alarmId alarm id to update.
     * @param users alarm users.
     */
    public void updateAlarm(final long alarmId, final List<Integer> users) {
        mDataBaseHelper.removeAlarmUsers(alarmId);
        for (final Integer user : users) {
            final ContentValues alarmUsersValues = new ContentValues();
            alarmUsersValues.put(DataBaseHelper.ALARM_ID, alarmId);
            alarmUsersValues.put(DataBaseHelper.USER_ID, user);
            mDataBaseHelper.insertAlarmUsers(alarmUsersValues);
        }
    }

    /**
     * Removes alarm.
     *
     * @param alarmId alarm id to remove.
     */
    public void removeAlarm(final int alarmId) {
        mDataBaseHelper.removeAlarm(alarmId);
        synchronized (mLock) {
            if (mAlarmUpdatedListener != null) {
                mAlarmUpdatedListener.onAlarmRemoved(alarmId);
            }
        }
    }

    /**
     * Removes alarm participant.
     *
     * @param alarmId alarm id to remove participant.
     * @param userId participant id.
     */
    public void removeAlarmParticipant(final int alarmId, final long userId) {
        mDataBaseHelper.removeAlarmUser(alarmId, userId);
        synchronized (mLock) {
            if (mAlarmUpdatedListener != null) {
                mAlarmUpdatedListener.onAlarmUpdated(alarmId);
            }
        }
    }

    /**
     * Returns <tt>true</tt> if current user is in one of alarms.
     *
     * @return <tt>true</tt> if current user is in one of alarms.
     */
    public boolean isMeInAlarm() {
        return mDataBaseHelper.isMeInAlarm();
    }

    /**
     * Returns cursor of friends.
     *
     * @param limit limit of friends.
     * @return cursor of friends.
     */
    private Cursor getFriends(final long limit) {
        return mDataBaseHelper.getUsers(limit);
    }

    /**
     * Returns alarm markers.
     *
     * @return alarm markers.
     */
    public List<AlarmMarker> getAlarmMarkers() {
        return mDataBaseHelper.getAlarmMarkers();
    }

    /**
     * Gets alarm marker.
     *
     * @param alarmId alarm id.
     * @return alarm marker.
     */
    public AlarmMarker getAlarmMarker(final long alarmId) {
        return mDataBaseHelper.getAlarmMarker(alarmId);
    }

    /**
     * Returns alarm users.
     *
     * @return alarm users (map key = user id, map value = list of alarms).
     */
    public LinkedHashMap<Integer, List<AlarmMarker>> getAlarmUsers() {
        return mDataBaseHelper.getAlarmUsers();
    }

    /**
     * Gets user name.
     *
     * @param id user id to get name.
     * @return user name.
     */
    public String getUserName(final int id) {
        return mDataBaseHelper.getUserName(id);
    }
}
