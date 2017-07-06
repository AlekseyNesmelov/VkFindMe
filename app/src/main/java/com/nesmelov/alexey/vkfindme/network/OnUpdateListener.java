package com.nesmelov.alexey.vkfindme.network;

import org.json.JSONObject;

public interface OnUpdateListener {
    void onUpdate(final int request, final JSONObject update);
    void onError(final int request, int errorCode);
}
