package com.nesmelov.alexey.vkfindme.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.nesmelov.alexey.vkfindme.services.GpsService;
import com.nesmelov.alexey.vkfindme.services.UpdateFriendsService;

/**
 * Boot broadcast receiver.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Intent gpsIntent = new Intent(context, GpsService.class);
        context.startService(gpsIntent);

        final Intent refreshIntent = new Intent(context, UpdateFriendsService.class);
        context.startService(refreshIntent);
    }
}
