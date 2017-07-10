package com.nesmelov.alexey.vkfindme.structures;

public class Alarm {
    private long mAlarmId;
    private double mLat;
    private double mLon;
    private float mRadius;

    public Alarm(final long alarmId, final double lat, final double lon, final float radius) {
        mAlarmId = alarmId;
        mLat = lat;
        mLon = lon;
        mRadius = radius;
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
}
