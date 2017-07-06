package com.nesmelov.alexey.vkfindme.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.nesmelov.alexey.vkfindme.services.GpsService;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Intent myIntent = new Intent(context, GpsService.class);
        context.startService(myIntent);
    }
}
