package com.nesmelov.alexey.vkfindme.structures;

import com.nesmelov.alexey.vkfindme.storage.Const;

public class User {
    protected Integer mVkId;
    protected String mName = "";
    protected String mSurname = "";
    protected Boolean mChecked = false;
    protected String mIconUrl = "";
    protected double mLat = Const.BAD_LAT;
    protected double mLon = Const.BAD_LON;
    private boolean mVisible = false;

    public User() {
    }

    public User(final Integer vkId, final String name, final String surname,
                final double lat, final double lon) {
        mVkId = vkId;
        mName = name;
        mSurname = surname;
        mLat = lat;
        mLon = lon;
    }

    public void setVkId(final Integer vkId) {
        mVkId = vkId;
    }

    public void setName(final String name) {
        mName = name;
    }

    public void setSurname(final String surname) {
        mSurname = surname;
    }

    public void setIconUrl(final String iconUrl) {
        mIconUrl = iconUrl;
    }

    public void setChecked(final boolean checked) {
        mChecked = checked;
    }

    public void setLat(final double lat) {
        mLat = lat;
    }

    public void setLon(final double lon) {
        mLon = lon;
    }

    public void setVisible(final boolean visible) {
        mVisible = visible;
    }

    public Integer getVkId() {
        return mVkId;
    }

    public String getName() {
        return mName;
    }

    public String getSurname() {
        return mSurname;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public boolean getChecked() {
        return mChecked;
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    public boolean isVisible() {
        return mVisible;
    }
}
