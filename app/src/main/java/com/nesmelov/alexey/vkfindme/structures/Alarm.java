package com.nesmelov.alexey.vkfindme.structures;

import java.util.ArrayList;

/**
 * Represents alarm object.
 */
public class Alarm {
    protected long mAlarmId;
    protected double mLat;
    protected double mLon;
    protected float mRadius;
    protected ArrayList<Integer> mUsers = new ArrayList<>();
    protected String mNames = "";

    /**
     * Constructs alarm instance.
     *
     * @param alarmId alarm id.
     * @param lat alarm latitude.
     * @param lon alarm longitude.
     * @param radius alarm radius.
     */
    public Alarm(final long alarmId, final double lat, final double lon, final float radius) {
        mAlarmId = alarmId;
        mLat = lat;
        mLon = lon;
        mRadius = radius;
    }

    /**
     * Constructs alarm instance.
     *
     * @param alarmId alarm id.
     * @param lat alarm latitude.
     * @param lon alarm longitude.
     * @param radius alarm radius.
     * @param users alarm users.
     * @param names alarm users names.
     */
    public Alarm(final long alarmId, final double lat, final double lon, final float radius,
                 final ArrayList<Integer> users, final String names) {
        this(alarmId, lat, lon, radius);
        mUsers = users;
        mNames = names;
    }

    /**
     * Gets alarm id.
     *
     * @return alarm id.
     */
    public long getAlarmId() {
        return mAlarmId;
    }

    /**
     * Gets alarm latitude.
     *
     * @return alarm latitude.
     */
    public double getLat() {
        return mLat;
    }

    /**
     * Gets alarm longitude.
     *
     * @return alarm longitude.
     */
    public double getLon() {
        return mLon;
    }

    /**
     * Gets alarm radius.
     *
     * @return alarm radius.
     */
    public float getRadius() {
        return mRadius;
    }

    /**
     * Gets alarm users.
     *
     * @return alarm users.
     */
    public ArrayList<Integer> getUsers() {
        return mUsers;
    }

    /**
     * Gets alarm user names.
     *
     * @return alarm user names.
     */
    public String getNames() {
        return mNames;
    }

    /**
     * Sets alarm users.
     *
     * @param users users to set.
     */
    public void setUsers(final ArrayList<Integer> users) {
        mUsers = users;
    }

    /**
     * Sets alarm users names.
     *
     * @param names alarm users names to set.
     */
    public void setNames(final String names) {
        mNames = names;
    }
}
