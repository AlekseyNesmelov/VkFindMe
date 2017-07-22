package com.nesmelov.alexey.vkfindme.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.structures.User;
import com.nesmelov.alexey.vkfindme.ui.marker.AlarmMarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String ID = "ID";
    public static final String USERS_TABLE = "USERS";
    public static final String PHOTO_URL = "URL";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String NAME = "NAME";
    public static final String SURNAME = "SURNAME";
    public static final String VK_ID = "VKID";
    public static final String VISIBLE = "VISIBLE";
    public static final String RADIUS = "RADIUS";
    public static final String CHECKED = "CHECKED";
    public static final String ALARM_TABLE = "ALARM";

    public static final String ALARM_USERS_TABLE = "AUSER";
    public static final String ALARM_ID = "AID";
    public static final String USER_ID = "UID";

    public DataBaseHelper(final Context context, final String name,
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

    public long insertUser(final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;

        final int vkId = values.getAsInteger(VK_ID);

        final StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT COUNT(*) FROM ").append(usersTable)
                .append(" WHERE ").append(VK_ID).append(" = ").append(vkId).append(";");

        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), null);

        boolean shouldInsert = true;
        if (cursor.moveToFirst()) {
            final int count = cursor.getInt(0);
            shouldInsert = count == 0;
        }

        if (shouldInsert) {
            return database.insert(usersTable, null, values);
        }/* else {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(PHOTO_URL, values.getAsString(PHOTO_URL));
            return updateUser(vkId, contentValues);
        }*/return -1;
    }

    public long insertAlarm(final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmTable = ALARM_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.insert(alarmTable, null, values);
    }

    public long insertAlarmUsers(final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.insert(alarmUsersTable, null, values);
    }

    public long removeAlarm(final long alarmId) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        database.delete(alarmUsersTable, ALARM_ID + "=" + alarmId, null);
        return database.delete(alarmTable, ID + "=" + alarmId, null);
    }

    public long removeAlarmUser(final long alarmId, final long alarmUser) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.delete(alarmUsersTable, ALARM_ID + "=" + alarmId + " AND "
                + USER_ID + "=" + alarmUser, null);
    }

    public long removeAlarmUsers(final long alarmId) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.delete(alarmUsersTable, ALARM_ID + "=" + alarmId, null);
    }

    public boolean isMeInAlarm() {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String alarmTable = ALARM_TABLE + "_" + user;
        final String selectQuery = "SELECT COUNT(*) FROM " + alarmUsersTable + ", " +
                alarmTable + " WHERE " + alarmTable + "." + ID + "=" +
                ALARM_ID + " AND " + CHECKED + "=1 AND " + USER_ID + "=" + user + ";";
        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(0) != 0;
        }
        return false;
    }

    public boolean isAlarmExist() {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        final String selectQuery = "SELECT COUNT(*) FROM " + alarmUsersTable +
                " WHERE NOT " + USER_ID + "=" + user + ";";
        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(0) != 0;
        }
        return false;
    }

    public List<Alarm> getAlarmsForMe() {
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

    public Map<Integer, List<Alarm>> getAlarmUsers() {
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

    public boolean isAlarmCompleted(final long alarmId) {
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

    public long updateUser(final Integer vkId, final ContentValues values) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.update(usersTable, values, VK_ID + "=" + vkId, null);
    }

    public long deleteUser(final String vkId) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.delete(usersTable, VK_ID + "=" + vkId, null);
    }

    public String getUserIdsString(final long limit) {
        final Integer user = FindMeApp.getStorage().getUserVkId();
        final String usersTable = USERS_TABLE + "_" + user;

        final StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT * FROM ").append(usersTable)
                .append(" WHERE NOT (").append(VK_ID).append(" = ").append(user)
                .append(") ORDER BY ").append(VISIBLE).append(" DESC LIMIT ").append(limit).append(";");

        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), null);

        final StringBuilder sb = new StringBuilder();
        if (cursor.moveToFirst()) {
            do {
                sb.append(cursor.getInt(1)).append(";");
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sb.toString();
    }

    public String getUserName(final int id) {
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

    public List<Integer> getUserIds(final long limit) {
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

    public List<User> getUsers(final long limit) {
        final List<User> users = new CopyOnWriteArrayList<>();

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

    public Map<Long, AlarmMarker> getAlarmMarkers(final Context context, final GoogleMap map) {
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
                        names.append(name + " " + surname).append(", ");
                    } while (usersCursor.moveToNext());
                }
                usersCursor.close();

                if (names.length() != 0) {
                    names = names.delete(names.length() - 2, names.length());
                }

                final AlarmMarker alarmMarker = new AlarmMarker(id, lat, lon,
                        radius, alarmUsers, names.toString());
                alarmMarker.addToMap(context, map);
                alarms.put(id, alarmMarker);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarms;
    }

    public AlarmMarker getAlarmMarker(final long alarmId) {
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
                        names.append(name + " " + surname).append(", ");
                    } while (usersCursor.moveToNext());
                }
                usersCursor.close();

                if (names.length() != 0) {
                    names = names.delete(names.length() - 2, names.length());
                }

                alarmMarker = new AlarmMarker(id, lat, lon,
                        radius, alarmUsers, names.toString());
                break;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarmMarker;
    }

    private void createUsersTable(final SQLiteDatabase db, final Integer user) {
        final String usersTable = USERS_TABLE + "_" + user;
        db.execSQL("CREATE TABLE " + usersTable + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + VK_ID + " INTEGER,"
                + NAME + " TEXT,"
                + SURNAME + " TEXT,"
                + LATITUDE + " REAL,"
                + LONGITUDE + " REAL,"
                + PHOTO_URL + " TEXT,"
                + VISIBLE + " INTEGER);");
    }

    private void createAlarmTables(final SQLiteDatabase db, final Integer user) {
        final String alarmTable = ALARM_TABLE + "_" + user;
        db.execSQL("CREATE TABLE " + alarmTable + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LATITUDE + " REAL,"
                + LONGITUDE + " REAL,"
                + RADIUS + " REAL,"
                + CHECKED + " INTEGER);");
        final String alarmUsersTable = ALARM_USERS_TABLE + "_" + user;
        db.execSQL("CREATE TABLE " + alarmUsersTable + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ALARM_ID + " INTEGER,"
                + USER_ID + " INTEGER);");
    }
}
