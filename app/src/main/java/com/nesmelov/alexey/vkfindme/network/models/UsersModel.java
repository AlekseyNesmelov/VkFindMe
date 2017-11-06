package com.nesmelov.alexey.vkfindme.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Users model.
 */
public class UsersModel {
    @SerializedName("users")
    @Expose
    private List<Integer> users;

    /**
     * Constructs users model.
     * @param users list of users.
     */
    public UsersModel(final List<Integer> users) {
        this.users = users;
    }

    /**
     * Gets users list.
     * @return users list.
     */
    public  List<Integer> getUsers() {
        return users == null ? new ArrayList<>() : users;
    }
}
