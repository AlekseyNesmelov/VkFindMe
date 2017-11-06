package com.nesmelov.alexey.vkfindme.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserModel {
    @SerializedName("user")
    @Expose
    protected Integer user;

    public UserModel(Integer user) {
        this.user = user;
    }

    public Integer getUser() {
        return user == null ? 0 : user;
    }
}
