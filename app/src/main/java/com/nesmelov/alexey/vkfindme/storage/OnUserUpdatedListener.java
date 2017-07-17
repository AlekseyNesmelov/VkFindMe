package com.nesmelov.alexey.vkfindme.storage;

public interface OnUserUpdatedListener {
    void onUserUpdated(final Integer userId, final double lat, final double lon);
    void onUserInvisible(final Integer userId);
}
