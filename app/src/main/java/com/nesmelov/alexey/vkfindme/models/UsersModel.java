package com.nesmelov.alexey.vkfindme.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UsersModel {
    @SerializedName("users")
    @Expose
    private List<UserModel> users;

    public UsersModel(final List<UserModel> users) {
        this.users = users;
    }

    public  List<UserModel> getUsers() {
        return users == null ? new ArrayList<>() : users;
    }
}
