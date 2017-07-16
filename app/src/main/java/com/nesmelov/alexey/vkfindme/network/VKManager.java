package com.nesmelov.alexey.vkfindme.network;

import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;

public class VKManager {
    public static final int REQUEST_GET_USER_INFO = 0;
    public static final int REQUEST_GET_FRIENDS = 1;

    public static final String RESPONSE = "response";
    public static final String ID = "id";
    public static final String PHOTO_MAX = "photo_max";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";

    public void executeRequest(final int request, final VKRequest.VKRequestListener listener) {
        switch (request) {
            case REQUEST_GET_USER_INFO:
                final VKRequest myProfileRequest = new VKRequest("users.get", VKParameters.from("fields", "photo_max"));
                myProfileRequest.executeWithListener(listener);
            break;
            case REQUEST_GET_FRIENDS:
                final VKRequest friendsRequest = new VKRequest("friends.get", VKParameters.from("order", "hints",
                        "fields", "photo_200"));
                friendsRequest.executeWithListener(listener);
                break;
        }
    }
}
