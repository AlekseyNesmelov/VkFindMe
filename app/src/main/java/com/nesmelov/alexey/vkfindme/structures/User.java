package com.nesmelov.alexey.vkfindme.structures;

import android.graphics.Bitmap;
import android.support.annotation.IntegerRes;

public class User {
    private Integer mVkId = 0;
    private String mName = "";
    private String mSurname = "";
    private Boolean mChecked = false;
    private Bitmap mIcon = null;
    private String mIconUrl = "";

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
}
