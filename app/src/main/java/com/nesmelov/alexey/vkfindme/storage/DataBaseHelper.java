package com.nesmelov.alexey.vkfindme.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nesmelov.alexey.vkfindme.application.FindMeApp;

import java.util.ArrayList;
import java.util.HashMap;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String ID = "ID";
    public static final String USERS_TABLE = "USER";
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

    private Storage mStorage;

    public DataBaseHelper(final Context context, final String name,
                          final SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, VERSION);
        mStorage = FindMeApp.getStorage();
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        final Integer user = mStorage.getUser();
        createUsersTable(db, user);
        createAlarmTables(db, user);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        final Integer user = mStorage.getUser();
        final String usersTable = user + "_" + USERS_TABLE;
        final String alarmTable = user + "_" + ALARM_TABLE;
        final String alarmUsersTable = user + "_" + ALARM_USERS_TABLE;
        db.execSQL("DROP TABLE IF EXISTS " + usersTable);
        db.execSQL("DROP TABLE IF EXISTS " + alarmTable);
        db.execSQL("DROP TABLE IF EXISTS " + alarmUsersTable);
        onCreate(db);
    }

    public long insertUser(final ContentValues values) {
        final Integer user = mStorage.getUser();
        final String usersTable = user + "_" + USERS_TABLE;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.insert(usersTable, null, values);
    }

    public long insertAlarm(final ContentValues values) {
        final Integer user = mStorage.getUser();
        final String alarmTable = user + "_" + ALARM_TABLE;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.insert(alarmTable, null, values);
    }

    public long insertAlarmUsers(final ContentValues values) {
        final Integer user = mStorage.getUser();
        final String alarmUsersTable = user + "_" + ALARM_USERS_TABLE;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.insert(alarmUsersTable, null, values);
    }

    public long updateUser(final Integer vkId, final ContentValues values) {
        final Integer user = mStorage.getUser();
        final String usersTable = user + "_" + USERS_TABLE;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.update(usersTable, values, VK_ID + "=" + vkId, null);
    }

    public long deleteUser(final String vkId) {
        final Integer user = mStorage.getUser();
        final String usersTable = user + "_" + USERS_TABLE;
        final SQLiteDatabase database = this.getWritableDatabase();
        return database.delete(usersTable, VK_ID + "=" + vkId, null);
    }

    public ArrayList<HashMap<String, String>> getAllFriends(final long limit) {
        final Integer user = mStorage.getUser();
        final String usersTable = user + "_" + USERS_TABLE;
        final ArrayList<HashMap<String, String>> records = new ArrayList<>();
        final StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT * FROM ").append(usersTable)
                .append(" WHERE NOT (").append(VK_ID).append(" = ").append(user)
                .append(") ORDER BY ").append(VISIBLE).append(" DESC LIMIT ").append(limit).append(";");
        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery.toString(), null);

        if (cursor.moveToFirst()) {
            do {
                final HashMap<String, String> map = new HashMap<String, String>();
                map.put(ID, cursor.getString(0));
                map.put(VK_ID, cursor.getString(1));
                map.put(NAME, cursor.getString(2));
                map.put(SURNAME, cursor.getString(3));
                map.put(LATITUDE, cursor.getString(4));
                map.put(LONGITUDE, cursor.getString(5));
                map.put(PHOTO_URL, cursor.getString(6));
                map.put(VISIBLE, cursor.getString(7));
                records.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return records;
    }

    /*public List<String> getUserIds(final long limit) {
        final List<String> result = new ArrayList();
        final String selectQuery = "SELECT " + Constant.VK_ID + " FROM " + Constant.FRIENDS_TABLE
                +" ORDER BY " + Constant.VISIBLE + " DESC" + ";";
        final SQLiteDatabase database = this.getWritableDatabase();
        final Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                final String vkId = cursor.getString(0);
                result.add(vkId);
            } while (cursor.moveToNext() && (result.size() < limit || limit == 0));
        }
        return result;
    }

    public void deleteAllRecords() {
        final List<String> records = getUserIds(Constant.FRIENDS_LIMIT);
        for (final String id : records) {
            deleteRecord(id);
        }
    }*/

    private void createUsersTable(final SQLiteDatabase db, final Integer user) {
        final String usersTable = user + "_" + USERS_TABLE;
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
        final String alarmTable = user + "_" + ALARM_TABLE;
        db.execSQL("CREATE TABLE " + alarmTable + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LATITUDE + " REAL,"
                + LONGITUDE + " REAL,"
                + RADIUS + " REAL,"
                + CHECKED + " INTEGER);");
        final String alarmUsersTable = user + "_" + ALARM_USERS_TABLE;
        db.execSQL("CREATE TABLE " + alarmUsersTable + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ALARM_ID + " INTEGER,"
                + USER_ID + " INTEGER);");
    }
}
