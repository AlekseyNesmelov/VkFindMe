package com.nesmelov.alexey.vkfindme.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StatusModel {
    public static final String OK = "ok";
    public static final String NOK = "nok";
    public static final String ALREADY_EXISTS = "already_exists";

    @SerializedName("status")
    @Expose
    private String status;

    public String getStatus() {
        return status == null ? "" : status;
    }
}
