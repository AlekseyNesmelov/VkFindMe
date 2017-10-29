package com.nesmelov.alexey.vkfindme.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class LatLonUsersModel {
    @SerializedName("users")
    @Expose
    private List<LatLonUserModel> users;

    public LatLonUsersModel(final List<LatLonUserModel> users) {
        this.users = users;
    }

    public  List<LatLonUserModel> getUsers() {
        return users == null ? new ArrayList<>() : users;
    }
}
