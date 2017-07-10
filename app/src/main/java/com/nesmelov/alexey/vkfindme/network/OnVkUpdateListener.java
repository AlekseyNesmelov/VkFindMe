package com.nesmelov.alexey.vkfindme.network;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

public interface OnVkUpdateListener {
    void onComplete(final VKResponse response);

    void onError(final VKError error);

    void attemptFailed(final VKRequest request, final int attemptNumber, final int totalAttempts);
}
