package com.nesmelov.alexey.vkfindme.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.nesmelov.alexey.vkfindme.storage.Storage;

/**
 * User position model.
 */
public class LatLonUserModel extends UserModel{
    @SerializedName("lat")
    @Expose
    protected Double lat;

    @SerializedName("lon")
    @Expose
    protected Double lon;

    /**
     * Constructs user position model.
     *
     * @param user user id.
     * @param lat latitude.
     * @param lon longitude.
     */
    public LatLonUserModel(final Integer user, final Double lat, final Double lon) {
        super(user);
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Gets latitude.
     *
     * @return latitude
     */
    public Double getLat() {
        return lat == null ? Storage.BAD_LAT : lat;
    }

    /**
     * Gets longitude.
     *
     * @return longitude
     */
    public Double getLon() {
        return lon == null ? Storage.BAD_LON : lon;
    }
}
