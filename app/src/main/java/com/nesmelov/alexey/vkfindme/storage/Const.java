package com.nesmelov.alexey.vkfindme.storage;

public class Const {
    public static final String USER = "user";
    public static final String USERS = "users";
    public static final String VISIBLE = "visible";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String RADIUS = "radius";
    public static final String ALARM_ID = "alarm_id";
    public static final String NAMES = "names";
    public static final String COLOR = "color";

    public static final String PLACE = "place";
    public static final String COORDINATES = "coordinates";

    public static final double BAD_LAT = -999;
    public static final double BAD_LON = -999;
    public static final long BAD_ID = -1;
    public static final int BAD_USER_ID = -1;
    public static final float BAD_RADIUS = 0;

    public static final int FRIENDS_LIMIT = 100;

    public static final int INVISIBLE_STATE = 0;
    public static final int VISIBLE_STATE = 1;

    public static final int RESULT_UPDATE = 2;

    public static final String PACKAGE_NAME =
            "com.nesmelov.alexey.vkfindme";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

    public static final int RESULT_OK = 0;
    public static final int RESULT_NOK = 1;
}
