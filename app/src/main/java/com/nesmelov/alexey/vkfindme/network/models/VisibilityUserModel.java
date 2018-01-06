package com.nesmelov.alexey.vkfindme.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * User visibility model.
 */
public class VisibilityUserModel extends UserModel{
    @SerializedName("visible")
    @Expose
    private Boolean visible;

    /**
     * Constructs user visibility model.
     *
     * @param user user id.
     * @param visible <tt>true</tt> if user is visible.
     */
    public VisibilityUserModel(final Integer user, final Boolean visible) {
        super(user);
        this.visible = visible;
    }

    /**
     * Gets user visibility.
     *
     * @return <tt>true</tt> if user is visible.
     */
    public Boolean getVisible() {
        return visible != null && visible;
    }
}
