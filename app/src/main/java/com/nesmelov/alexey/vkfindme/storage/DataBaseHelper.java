package com.nesmelov.alexey.vkfindme.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.models.UserModel;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.structures.User;
import com.nesmelov.alexey.vkfindme.ui.marker.AlarmMarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Data base helper class.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String ID = "ID";
    private static final String USERS_TABLE = "USERS";
    static final String PHOTO_URL = "URL";
    static final String LATITUDE = "LATITUDE";
    static final String LONGITUDE = "LONGITUDE";
    static final String NAME = "NAME";
    static final String SURNAME = "SURNAME";
    static final String VK_ID = "VKID";
    static final String VISIBLE = "VISIBLE";
    static final String RADIUS = "RADIUS";
    static final String CHECKED = "CHECKED";
    private static final String ALARM_TABLE = "ALARM";
    static final String COLOR = "COLOR";

    private static final String ALARM_USERS_TABLE = "AUSER";
    static final String ALARM_ID = "AID";
    static final String USER_ID = "UID";

    /**
     * Constructs database helper instance.
     *
     * @param context context to use.
     * @param name database name.
     * @param factory cursor factory.
     */
    DataBaseHelper(final Context context, final String name,
                   final SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        createUsersTable(db, user);
        createAlarmTables(db, user);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        db.execSQL("DROP TABLE IF EXISTS " + usersTable);
        db.execSQL("DROP TABLE IF EXISTS " + alarmTable);
        db.execSQL("DROP TABLE IF EXISTS " + alarmUsersTable);
        onCreate(db);
    }

    /**
     * Inserts user.
     *
     * @param values user values.
     * @return id of inserted row.
     */
    long insertUser(final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;

        final int vkId = values.getAsInteger(VK_ID);

        String selectQuery = "SELECT COUNT(*) FROM " + usersTable +
                " WHERE " + VK_ID + " = " + vkId + ";";

        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, null);

        boolean shouldInsert = true;
        if (cursor.moveToFirst()) {
            final int count = cursor.getInt(0);
            shouldInsert = count == 0;
        }
        cursor.close();

        return shouldInsert ? database.insert(usersTable, null, values) : -1;
    }

    /**
     * Inserts alarm.
     *
     * @param values alarm values.
     * @return id of inserted alarm.
     */
    long insertAlarm(final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmTable = ALARM_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.insert(alarmTable, null, values);
    }

    /**
     * Inserts alarm users.
     *
     * @param values values to insert.
     * @return id of inserted row.
     */
    long insertAlarmUsers(final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.insert(alarmUsersTable, null, values);
    }

    /**
     * Removes alarm.
     *
     * @param alarmId alarm id to remove.
     * @return number of affected rows.
     */
    long removeAlarm(final long alarmId) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        database.delete(alarmUsersTable, ALARM_ID + "=" + alarmId, null);
        return database.delete(alarmTable, ID + "=" + alarmId, null);
    }

    /**
     * Removes alarm user.
     *
     * @param alarmId alarm id.
     * @param alarmUser user id.
     * @return number of affected rows.
     */
    long removeAlarmUser(final long alarmId, final long alarmUser) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.delete(alarmUsersTable, ALARM_ID + "=" + alarmId + " AND "
                + USER_ID + "=" + alarmUser, null);
    }

    /**
     * Removes alarm users.
     *
     * @param alarmId alarm id.
     * @return number of affected rows.
     */
    long removeAlarmUsers(final long alarmId) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.delete(alarmUsersTable, ALARM_ID + "=" + alarmId, null);
    }

    /**
     * Returns <tt>true</tt> if current user is in alarm.
     *
     * @return <tt>true</tt> if current user is in alarm.
     */
    boolean isMeInAlarm() {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final String selectQuery = "SELECT COUNT(*) FROM " + alarmUsersTable + ", " +
                alarmTable + " WHERE " + alarmTable + "." + ID + "=" +
                ALARM_ID + " AND " + CHECKED + "=1 AND " + USER_ID + "=" + user + ";";
        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, null);

        final boolean result = cursor.moveToFirst() && cursor.getLong(0) != 0;
        cursor.close();
        return result;
    }

    /**
     * Returns <tt>true</tt> friends alarm exists.
     *
     * @return <tt>true</tt> friends alarm exists.
     */
    boolean isAlarmExist() {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String selectQuery = "SELECT COUNT(*) FROM " + alarmUsersTable +
                " WHERE NOT " + USER_ID + "=" + user + ";";
        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, null);

        final boolean result = cursor.moveToFirst() && cursor.getLong(0) != 0;
        cursor.close();
        return result;
    }

    /**
     * Returns list of alarms, where user takes part in.
     *
     * @return list of alarms, where user takes part in.
     */
    List<Alarm> getAlarmsForMe() {
        final List<Alarm> alarms = new ArrayList<>();

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT * FROM ").append(alarmTable).append(", ")
                .append(alarmUsersTable).append(" WHERE ").append(alarmTable)
                .append(".").append(ID).append("=").append(ALARM_ID).append(" AND ")
                .append(CHECKED).append("=1 AND ").append(USER_ID).append("=").append(user).append(";");
        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), null);
        if (cursor.moveToFirst()) {
            do {
                final Alarm alarm = new Alarm(
                        cursor.getInt(0),
                        cursor.getDouble(1),
                        cursor.getDouble(2),
                        cursor.getFloat(3)
                );
                alarms.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarms;
    }

    /**
     * Returns alarm users.
     *
     * @return alarm users.
     */
    Map<Integer, List<Alarm>> getAlarmUsers() {
        final Map<Integer, List<Alarm>> alarmUsers = new ConcurrentHashMap<>();

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmTable = ALARM_TABLE + "_" + user;
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;

        final StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT ")
                .append(alarmUsersTable).append(".").append(USER_ID).append(", ")

                .append(alarmTable).append(".").append(ID).append(", ")
                .append(alarmTable).append(".").append(LATITUDE).append(", ")
                .append(alarmTable).append(".").append(LONGITUDE).append(", ")
                .append(alarmTable).append(".").append(RADIUS)

                .append(" FROM ")
                .append(alarmUsersTable).append(", ")
                .append(alarmTable)

                .append(" WHERE ")
                .append(alarmTable).append(".").append(ID).append("=").append(ALARM_ID)
                .append(";");

        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), null);

        if (cursor.moveToFirst()) {
            do {
                final int userVkId = cursor.getInt(0);
                final int alarmId = cursor.getInt(1);
                final double lat = cursor.getDouble(2);
                final double lon = cursor.getDouble(3);
                final float radius = cursor.getFloat(4);

                final Alarm alarm = new Alarm(alarmId, lat, lon, radius);

                List<Alarm> alarms = alarmUsers.get(userVkId);
                if (alarms == null) {
                    alarms = new ArrayList<>();
                }
                alarms.add(alarm);

                alarmUsers.put(userVkId, alarms);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarmUsers;
    }

    /**
     * Returns <tt>true</tt> if alarm is completed.
     *
     * @param alarmId alarm id to check.
     * @return <tt>true</tt> if alarm is completed.
     */
    boolean isAlarmCompleted(final long alarmId) {
        boolean isCompleted = true;

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String selectQuery = "SELECT COUNT(*) FROM " + alarmUsersTable +
                " WHERE " + ALARM_ID + "=" + alarmId + ";";
        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            isCompleted =  cursor.getLong(0) == 0;
        }
        cursor.close();
        return isCompleted;
    }

    /**
     * Updates user.
     *
     * @param vkId user id.
     * @param values user values.
     * @return number of affected rows.
     */
    long updateUser(final Integer vkId, final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.update(usersTable, values, VK_ID + "=" + vkId, null);
    }

    /**
     * Deletes user.
     *
     * @param vkId user id.
     * @return number of affected rows.
     */
    public long deleteUser(final String vkId) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.delete(usersTable, VK_ID + "=" + vkId, null);
    }

    /**
     * Gets user name.
     *
     * @param id user id to get name.
     * @return user name.
     */
    String getUserName(final int id) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;

        final StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT ").append(NAME).append(", ").append(SURNAME)
                .append(" FROM ").append(usersTable)
                .append(" WHERE ").append(VK_ID).append(" = ").append(id).append(";");

        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), null);

        if (cursor.moveToFirst()) {
            do {
                return cursor.getString(0) + " " + cursor.getString(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return "";
    }

    /**
     * Gets list of users ids.
     *
     * @param limit limit of users.
     * @return list of users ids.
     */
    List<Integer> getUserIds(final long limit) {
        final List<Integer> users = new ArrayList<>();

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;

        final StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT * FROM ").append(usersTable)
                .append(" WHERE NOT (").append(VK_ID).append(" = ").append(user)
                .append(") ORDER BY ").append(VISIBLE).append(" DESC LIMIT ").append(limit).append(";");

        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), null);

        if (cursor.moveToFirst()) {
            do {
                users.add(cursor.getInt(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    /**
     * Gets list of users.
     *
     * @param limit limit of users.
     * @return list of users.
     */
    List<User> getUsers(final long limit) {
        final List<User> users = new CopyOnWriteArrayList<>();

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;

        final String selectQuery = "SELECT * FROM " + usersTable +
                " WHERE NOT (" + VK_ID + " = " + user +
                ") ORDER BY " + VISIBLE + " DESC LIMIT " + limit + ";";

        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                final User friend = new User();
                friend.setVkId(cursor.getInt(1));
                friend.setName(cursor.getString(2));
                friend.setSurname(cursor.getString(3));
                friend.setLat(cursor.getDouble(4));
                friend.setLon(cursor.getDouble(5));
                friend.setIconUrl(cursor.getString(6));
                friend.setVisible(cursor.getInt(7) == 1);
                users.add(friend);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    /**
     * Gets users models.
     *
     * @param limit limit of users.
     * @return users models.
     */
    List<UserModel> getUserModels(final long limit) {
        final List<UserModel> users = new CopyOnWriteArrayList<>();

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;

        String selectQuery = "SELECT * FROM " + usersTable +
                " WHERE NOT (" + VK_ID + " = " + user +
                ") ORDER BY " + VISIBLE + " DESC LIMIT " + limit + ";";

        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                final UserModel friend = new UserModel(cursor.getInt(1));
                users.add(friend);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    /**
     * Returns alarm markers.
     *
     * @return alarm markers.
     */
    Map<Long, AlarmMarker> getAlarmMarkers() {
        final Map<Long, AlarmMarker> alarms = new ConcurrentHashMap<>();

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;

        StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT * FROM ").append(alarmTable).append(";");

        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), null);

        if (cursor.moveToFirst()) {
            do {
                final long id = cursor.getLong(0);
                final double lat = cursor.getDouble(1);
                final double lon = cursor.getDouble(2);
                final float radius = cursor.getFloat(3);
                final int color = cursor.getInt(5);

                selectQuery = new StringBuilder();
                selectQuery.append("SELECT ").append (VK_ID).append(", ")
                .append(NAME).append(", ").append(SURNAME).append(" FROM ")
                        .append(alarmUsersTable).append(", ").append(usersTable)
                        .append(" WHERE ").append(USER_ID).append("=").append(VK_ID)
                        .append(" AND ").append(ALARM_ID).append("=").append(id).append(";");

                final Cursor usersCursor = database.rawQuery(selectQuery.toString(), null);

                final ArrayList<Integer> alarmUsers = new ArrayList<>();
                StringBuilder names = new StringBuilder();
                if (usersCursor.moveToFirst()) {
                    do {
                        final int vkId =  usersCursor.getInt(0);
                        final String name =  usersCursor.getString(1);
                        final String surname =  usersCursor.getString(2);

                        alarmUsers.add(vkId);
                        names.append(name).append(" ").append(surname).append(", ");
                    } while (usersCursor.moveToNext());
                }
                usersCursor.close();

                if (names.length() != 0) {
                    names = names.delete(names.length() - 2, names.length());
                }

                final AlarmMarker alarmMarker = new AlarmMarker(id, lat, lon,
                        radius, color, alarmUsers, names.toString());
                alarms.put(id, alarmMarker);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarms;
    }

    /**
     * Gets alarm marker.
     * @param alarmId alarm id.
     * @return alarm marker.
     */
    AlarmMarker getAlarmMarker(final long alarmId) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;

        StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT * FROM ").append(alarmTable)
                .append(" WHERE ").append(ID).append("=").append(alarmId).append(";");

        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), null);

        AlarmMarker alarmMarker = null;
        if (cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(0);
                final double lat = cursor.getDouble(1);
                final double lon = cursor.getDouble(2);
                final float radius = cursor.getFloat(3);
                final int color = cursor.getInt(5);

                selectQuery = new StringBuilder();
                selectQuery.append("SELECT ").append (VK_ID).append(", ")
                        .append(NAME).append(", ").append(SURNAME).append(" FROM ")
                        .append(alarmUsersTable).append(", ").append(usersTable)
                        .append(" WHERE ").append(USER_ID).append("=").append(VK_ID)
                        .append(" AND ").append(ALARM_ID).append("=").append(id).append(";");

                final Cursor usersCursor = database.rawQuery(selectQuery.toString(), null);

                final ArrayList<Integer> alarmUsers = new ArrayList<>();
                StringBuilder names = new StringBuilder();
                if (usersCursor.moveToFirst()) {
                    do {
                        final int vkId =  usersCursor.getInt(0);
                        final String name =  usersCursor.getString(1);
                        final String surname =  usersCursor.getString(2);

                        alarmUsers.add(vkId);
                        names.append(name).append(" ").append(surname).append(", ");
                    } while (usersCursor.moveToNext());
                }
                usersCursor.close();

                if (names.length() != 0) {
                    names = names.delete(names.length() - 2, names.length());
                }

                alarmMarker = new AlarmMarker(id, lat, lon,
                        radius, color, alarmUsers, names.toString());
                break;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarmMarker;
    }

    /**
     * Recreates database tables.
     *
     * @param db database.
     */
    public void recreateTables(final SQLiteDatabase db) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        createUsersTable(db, user);
        createAlarmTables(db, user);
    }

    /**
     * Recreates database tables.
     */
    public void recreateTables() {
        final SQLiteDatabase db = this.getWritableDatabase();
        final Integer user = FindMeApp.getStorage().getUserVkId();
        createUsersTable(db, user);
        createAlarmTables(db, user);
    }

    /**
     * Creates users table.
     *
     * @param db database.
     * @param user user id.
     */
    private void createUsersTable(final SQLiteDatabase db, final Integer user) {
        final String usersTable = USERS_TABLE + "_" + user;
        db.execSQL("CREATE TABLE IF NOT EXISTS " + usersTable + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + VK_ID + " INTEGER,"
                + NAME + " TEXT,"
                + SURNAME + " TEXT,"
                + LATITUDE + " REAL,"
                + LONGITUDE + " REAL,"
                + PHOTO_URL + " TEXT,"
                + VISIBLE + " INTEGER);");
    }

    /**
     * Creates alarm table.
     *
     * @param db database.
     * @param user user id.
     */
    private void createAlarmTables(final SQLiteDatabase db, final Integer user) {
        final String alarmTable = ALARM_TABLE + "_" + user;
        db.execSQL("CREATE TABLE IF NOT EXISTS " + alarmTable + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LATITUDE + " REAL,"
                + LONGITUDE + " REAL,"
                + RADIUS + " REAL,"
                + CHECKED + " INTEGER,"
                + COLOR + " INTEGER);");
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        db.execSQL("CREATE TABLE IF NOT EXISTS " + alarmUsersTable + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ALARM_ID + " INTEGER,"
                + USER_ID + " INTEGER);");
    }
}
