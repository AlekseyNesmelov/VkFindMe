package com.nesmelov.alexey.vkfindme.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LatLonUserModel {
    @SerializedName("user")
    @Expose
    private Integer user;

    @SerializedName("lat")
    @Expose
    private Double lat;

    @SerializedName("lon")
    @Expose
    private Double lon;

    public LatLonUserModel(final Integer user, final Double lat, final Double lon) {
        this.user = user;
        this.lat = lat;
        this.lon = lon;
    }

    public Integer getUser() {
        return user == null ? 0 : user;
    }

    public Double getLat() {
        return lat == null ? 0 : lat;
    }

    public Double getLon() {
        return lon == null ? 0 : lon;
    }
}
