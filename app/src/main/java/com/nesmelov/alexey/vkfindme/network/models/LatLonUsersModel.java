package com.nesmelov.alexey.vkfindme.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents list of user position models.
 */
public class LatLonUsersModel {
    @SerializedName("users")
    @Expose
    private List<LatLonUserModel> users;

    /**
     * Constructs user position list model.
     *
     * @param users list of user positions.
     */
    public LatLonUsersModel(final List<LatLonUserModel> users) {
        this.users = users;
    }

    /**
     * Gets list of user positions.
     *
     * @return list of user positions.
     */
    public  List<LatLonUserModel> getUsers() {
        return users == null ? new ArrayList<>() : users;
    }
}
