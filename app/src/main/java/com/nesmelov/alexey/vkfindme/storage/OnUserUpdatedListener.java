package com.nesmelov.alexey.vkfindme.storage;

public interface OnUserUpdatedListener {
    void onUserUpdated(final long userId, final double lat, final double lon);
    void onUserInvisible(final long userId);
}
