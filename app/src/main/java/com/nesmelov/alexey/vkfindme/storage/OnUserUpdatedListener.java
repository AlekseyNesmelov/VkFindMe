package com.nesmelov.alexey.vkfindme.storage;

/**
 * User updated event listener.
 */
public interface OnUserUpdatedListener {
    /**
     * This method is called when an user is updated.
     *
     * @param userId user id.
     * @param lat user latitude.
     * @param lon user longitude.
     */
    void onUserUpdated(final Integer userId, final double lat, final double lon);

    /**
     * This method is called when user becomes invisible.
     *
     * @param userId user id.
     */
    void onUserInvisible(final Integer userId);
}
