package com.nesmelov.alexey.vkfindme.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Model that represents user VK id.
 */
public class UserModel {
    @SerializedName("user")
    @Expose
    protected Integer user;

    /**
     * Constructs user id model.
     *
     * @param user user id.
     */
    public UserModel(final Integer user) {
        this.user = user;
    }

    /**
     * Gets user id.
     *
     * @return user id.
     */
    public Integer getUser() {
        return user == null ? 0 : user;
    }
}
