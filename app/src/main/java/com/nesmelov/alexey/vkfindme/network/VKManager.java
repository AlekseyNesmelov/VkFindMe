package com.nesmelov.alexey.vkfindme.network;

import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;

/**
 * VK requests manager.
 */
public class VKManager {
    public static final String RESPONSE = "response";
    public static final String ID = "id";
    public static final String PHOTO_200 = "photo_200";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";

    /**
     * Gets user info.
     * @param listener response listener.
     */
    public void getUserInfo(final VKRequest.VKRequestListener listener) {
        final VKRequest myProfileRequest = new VKRequest("users.get", VKParameters.from("fields", "photo_200"));
        myProfileRequest.executeWithListener(listener);
    }

    /**
     * Gets friends.
     * @param listener response listener.
     */
    public void getFriends(final VKRequest.VKRequestListener listener) {
        final VKRequest friendsRequest = new VKRequest("friends.get", VKParameters.from("order", "hints",
                "fields", "photo_200"));
        friendsRequest.executeWithListener(listener);
    }
}
