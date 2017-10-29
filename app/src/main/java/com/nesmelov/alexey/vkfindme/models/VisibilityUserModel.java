package com.nesmelov.alexey.vkfindme.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VisibilityUserModel {
    @SerializedName("user")
    @Expose
    private Integer user;

    @SerializedName("visible")
    @Expose
    private Boolean visible;

    public VisibilityUserModel(final Integer user, final Boolean visible) {
        this.user = user;
        this.visible = visible;
    }

    public Integer getUser() {
        return user == null ? 0 : user;
    }

    public Boolean getVisible() {
        return visible != null && visible;
    }
}
