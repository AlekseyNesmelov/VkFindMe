package com.nesmelov.alexey.vkfindme.structures;

import java.util.ArrayList;

public class Alarm {
    protected long mAlarmId;
    protected double mLat;
    protected double mLon;
    protected float mRadius;
    protected ArrayList<Integer> mUsers = new ArrayList<>();
    protected String mNames = "";

    public Alarm(final long alarmId, final double lat, final double lon, final float radius) {
        mAlarmId = alarmId;
        mLat = lat;
        mLon = lon;
        mRadius = radius;
    }

    public Alarm(final long alarmId, final double lat, final double lon, final float radius,
                 final ArrayList<Integer> users, final String names) {
        this(alarmId, lat, lon, radius);
        mUsers = users;
        mNames = names;
    }

    public long getAlarmId() {
        return mAlarmId;
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    public float getRadius() {
        return mRadius;
    }

    public ArrayList<Integer> getUsers() {
        return mUsers;
    }

    public String getNames() {
        return mNames;
    }

    public void setUsers(final ArrayList<Integer> users) {
        mUsers = users;
    }

    public void setNames(final String names) {
        mNames = names;
    }
}
