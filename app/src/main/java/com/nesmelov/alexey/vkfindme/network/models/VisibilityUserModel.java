package com.nesmelov.alexey.vkfindme.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VisibilityUserModel extends UserModel{
    @SerializedName("visible")
    @Expose
    private Boolean visible;

    public VisibilityUserModel(final Integer user, final Boolean visible) {
        super(user);
        this.visible = visible;
    }

    public Boolean getVisible() {
        return visible != null && visible;
    }
}
