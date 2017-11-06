package com.nesmelov.alexey.vkfindme.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Full user info model.
 */
public class FullUserInfoModel extends LatLonUserModel{
    @SerializedName("visible")
    @Expose
    private Boolean visible;

    /**
     * Constructs full user info model.
     * @param user user id.
     * @param lat latitude.
     * @param lon longitude.
     * @param visible visibility.
     */
    public FullUserInfoModel(final Integer user, final Double lat, final Double lon, final Boolean visible) {
        super(user, lat, lon);
        this.visible = visible;
    }

    /**
     * Returns user visibility.
     * @return user visibility.
     */
    public Boolean getVisible() {
        return visible != null && visible;
    }
}
