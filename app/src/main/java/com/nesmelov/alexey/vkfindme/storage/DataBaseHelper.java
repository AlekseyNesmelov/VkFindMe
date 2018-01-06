package com.nesmelov.alexey.vkfindme.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.ui.markers.AlarmMarker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data base helper class.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String ID = "ID";
    private static final String USERS_TABLE = "USERS";
    private static final String ALARM_TABLE = "ALARM";
    private static final String ALARM_USERS_TABLE = "AUSER";
    static final String PHOTO_URL = "URL";
    static final String LATITUDE = "LATITUDE";
    static final String LONGITUDE = "LONGITUDE";
    static final String NAME = "NAME";
    static final String SURNAME = "SURNAME";
    static final String VK_ID = "VKID";
    static final String VISIBLE = "VISIBLE";
    static final String RADIUS = "RADIUS";
    static final String CHECKED = "CHECKED";
    static final String COLOR = "COLOR";
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
    int insertUser(final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;

        final int vkId = values.getAsInteger(VK_ID);

        final String selectQuery = "SELECT COUNT(*) FROM " + usersTable + " WHERE " + VK_ID + " = ?;";

        final SQLiteDatabase database = getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, new String[] {String.valueOf(vkId)});

        boolean shouldInsert = true;
        if (cursor.moveToFirst()) {
            final int count = cursor.getInt(0);
            shouldInsert = count == 0;
        }
        cursor.close();

        return shouldInsert ? (int)database.insert(usersTable, null, values) : -1;
    }

    /**
     * Inserts alarm.
     *
     * @param values alarm values.
     * @return id of inserted alarm.
     */
    int insertAlarm(final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmTable = ALARM_TABLE + "_" + user;
        final SQLiteDatabase database = getWritableDatabase();
        return (int)database.insert(alarmTable, null, values);
    }

    /**
     * Inserts alarm users.
     *
     * @param values values to insert.
     */
    void insertAlarmUsers(final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final SQLiteDatabase database = getWritableDatabase();
        database.insert(alarmUsersTable, null, values);
    }

    /**
     * Removes alarm.
     *
     * @param alarmId alarm id to remove.
     */
    void removeAlarm(final long alarmId) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(alarmUsersTable, ALARM_ID + "= ?", new String[] {String.valueOf(alarmId)});
        database.delete(alarmTable, ID + "= ?", new String[] {String.valueOf(alarmId)});
    }

    /**
     * Removes alarm user.
     *
     * @param alarmId alarm id.
     * @param alarmUser user id.
     */
    void removeAlarmUser(final long alarmId, final long alarmUser) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(alarmUsersTable, ALARM_ID + "= ? AND "
                + USER_ID + "= ?", new String[] {String.valueOf(alarmId), String.valueOf(alarmUser)});
    }

    /**
     * Removes alarm users.
     *
     * @param alarmId alarm id.
     */
    void removeAlarmUsers(final long alarmId) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(alarmUsersTable, ALARM_ID + "= ?", new String[] {String.valueOf(alarmId)});
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
        final String selectQuery = "SELECT COUNT(*) FROM " + alarmUsersTable
                + ", " + alarmTable + " WHERE " + alarmTable + "." + ID + "=" +
                ALARM_ID + " AND " + CHECKED + "=1 AND " + USER_ID + "= ?;";
        final SQLiteDatabase database = getReadableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery,
                new String[] {user.toString()});
        final boolean result = cursor.moveToFirst() && cursor.getLong(0) != 0;
        cursor.close();
        return result;
    }

    /**
     * Returns <tt>true</tt> friends alarm exists.
     *
     * @return <tt>true</tt> friends alarm exists.
     */
    boolean isAlarmForFriendExist() {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String selectQuery = "SELECT COUNT(*) FROM " + alarmUsersTable + " WHERE NOT " + USER_ID + "= ?;";
        final SQLiteDatabase database = getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, new String[] {user.toString()});
        final boolean result = cursor.moveToFirst() && cursor.getLong(0) != 0;
        cursor.close();
        return result;
    }

    /**
     * Returns list of alarms, where user takes part in.
     *
     * @return list of alarms, where user takes part in.
     */
    List<AlarmMarker> getAlarmsForMe() {
        final List<AlarmMarker> alarms = new ArrayList<>();

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final String selectQuery = "SELECT * FROM " + alarmTable + ", " + alarmUsersTable
                + " WHERE " + alarmTable + "." + ID + "=" + ALARM_ID + " AND " +
                CHECKED + "=1 AND " + USER_ID + "= ?;";
        final SQLiteDatabase database = getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, new String[] {user.toString()});
        if (cursor.moveToFirst()) {
            do {
                final AlarmMarker alarm = new AlarmMarker(
                        cursor.getInt(0),
                        cursor.getDouble(1),
                        cursor.getDouble(2),
                        cursor.getFloat(3),
                        cursor.getInt(5)
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
     * @return alarm users (map key = user id, map value = list of alarms).
     */
    LinkedHashMap<Integer, List<AlarmMarker>> getAlarmUsers() {
        final LinkedHashMap<Integer, List<AlarmMarker>> alarmUsers = new LinkedHashMap<>();

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmTable = ALARM_TABLE + "_" + user;
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;

        final String selectQuery = "SELECT " +
                alarmUsersTable + "." + USER_ID + ", " +
                alarmTable + "." + ID + ", " +
                alarmTable + "." + LATITUDE + ", " +
                alarmTable + "." + LONGITUDE + ", " +
                alarmTable + "." + RADIUS + ", " +
                alarmTable + "." + COLOR +
                " FROM " +
                alarmUsersTable + ", " +
                alarmTable +
                " WHERE " +
                alarmTable + "." + ID + "=" + ALARM_ID + ";";

        final SQLiteDatabase database = getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                final int userVkId = cursor.getInt(0);
                final int alarmId = cursor.getInt(1);
                final double lat = cursor.getDouble(2);
                final double lon = cursor.getDouble(3);
                final float radius = cursor.getFloat(4);
                final int color = cursor.getInt(5);

                final AlarmMarker alarm = new AlarmMarker(alarmId, lat, lon, radius, color);

                List<AlarmMarker> alarms = alarmUsers.get(userVkId);
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
    boolean isAlarmCompleted(final int alarmId) {
        boolean isCompleted = false;

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String selectQuery = "SELECT COUNT(*) FROM " + alarmUsersTable +
                " WHERE " + ALARM_ID + "= ?;";
        final SQLiteDatabase database = getReadableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, new String[] {String.valueOf(alarmId)});

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
     */
    void updateUser(final Integer vkId, final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;
        final SQLiteDatabase database = getWritableDatabase();
        database.update(usersTable, values, VK_ID + "= ?", new String[] {String.valueOf(vkId)});
    }

    /**
     * Deletes user.
     *
     * @param vkId user id.
     */
    void deleteUser(final int vkId) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(usersTable, VK_ID + "= ?", new String[] {String.valueOf(vkId)});
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

        final String selectQuery = "SELECT " + NAME + ", " + SURNAME +
                " FROM " + usersTable +
                " WHERE " + VK_ID + " = ?;";

        final SQLiteDatabase database = getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, new String[] {String.valueOf(id)});

        String result = null;
        if (cursor.moveToFirst()) {
            result = cursor.getString(0) + " " + cursor.getString(1);
        }
        cursor.close();
        return result;
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

        final String selectQuery = "SELECT " + VK_ID + " FROM " + usersTable +
                " WHERE NOT (" + VK_ID + " = ?" +
                ") ORDER BY " + VISIBLE + " DESC LIMIT ?;";

        final SQLiteDatabase database = getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, new String[] {String.valueOf(user), String.valueOf(limit)});

        if (cursor.moveToFirst()) {
            do {
                users.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    /**
     * Gets list of users.
     *
     * @param limit limit of users.
     * @return database cursor, that contains users.
     */
    Cursor getUsers(final long limit) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;

        final String selectQuery = "SELECT * FROM " + usersTable +
                " WHERE NOT (" + VK_ID + " = ?) ORDER BY " + VISIBLE + " DESC LIMIT ?;";
        final SQLiteDatabase database = getReadableDatabase();

        return database.rawQuery(selectQuery, new String[] {user.toString(), String.valueOf(limit) });
    }

    /**
     * Returns alarm markers.
     *
     * @return alarm markers.
     */
    List<AlarmMarker> getAlarmMarkers() {
        final List<AlarmMarker> alarms = new ArrayList<>();

        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;

        StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT * FROM ").append(alarmTable).append(";");

        final SQLiteDatabase database = getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), null);

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
                        .append(" AND ").append(ALARM_ID).append("= ?;");

                final Cursor usersCursor = database.rawQuery(selectQuery.toString(), new String[] {String.valueOf(id)});

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

                final AlarmMarker alarmMarker = new AlarmMarker(id, lat, lon, radius, color, alarmUsers, names.toString());
                alarms.add(alarmMarker);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarms;
    }

    /**
     * Gets alarm marker.
     *
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
                .append(" WHERE ").append(ID).append("= ?;");

        final SQLiteDatabase database = getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), new String[] {String.valueOf(alarmId)});

        AlarmMarker alarmMarker = null;
        if (cursor.moveToFirst()) {
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
                    .append(" AND ").append(ALARM_ID).append("= ?;");

            final Cursor usersCursor = database.rawQuery(selectQuery.toString(), new String[] {String.valueOf(id)});

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

            alarmMarker = new AlarmMarker(id, lat, lon, radius, color, alarmUsers, names.toString());
        }
        cursor.close();
        return alarmMarker;
    }

    /**
     * Recreates database tables.
     */
    void recreateTables() {
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
