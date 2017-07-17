package com.nesmelov.alexey.vkfindme.structures;

import android.graphics.Bitmap;
import android.support.annotation.IntegerRes;

import com.nesmelov.alexey.vkfindme.storage.Const;

public class User {
    private Integer mVkId;
    private String mName = "";
    private String mSurname = "";
    private Boolean mChecked = false;
    private Bitmap mIcon = null;
    private String mIconUrl = "";
    private double mLat = Const.BAD_LAT;
    private double mLon = Const.BAD_LON;

    public void setVkId(final Integer vkId) {
        mVkId = vkId;
    }

    public void setName(final String name) {
        mName = name;
    }

    public void setSurname(final String surname) {
        mSurname = surname;
    }

    public void setIcon(final Bitmap icon) {
        mIcon = icon;
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

    public Integer getVkId() {
        return mVkId;
    }

    public String getName() {
        return mName;
    }

    public String getSurname() {
        return mSurname;
    }

    public Bitmap getIcon() {
        return mIcon;
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
}
