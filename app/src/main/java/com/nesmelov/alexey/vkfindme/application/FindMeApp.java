package com.nesmelov.alexey.vkfindme.application;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.storage.Storage;

public class FindMeApp extends Application {
    private static HTTPManager sHTTPManager;
    private static Storage sStorage;
    private static NotificationManager sNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sHTTPManager = new HTTPManager(this);
        sStorage = new Storage(this);
        sNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public static HTTPManager getHTTPManager() {
        return sHTTPManager;
    }

    public static Storage getStorage() {
        return sStorage;
    }

    public static void showPopUp(final Context context, final String title, final String message) {
        final AlertDialog adb = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        adb.show();
    }

    public static void showToast(final Context context, final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void displayNotification(final int id, final Context context,
                                           final String ticket, final String message, final Class<?> pendingClass) {
        final Intent intent = new Intent(context, pendingClass);
        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        final Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(ticket)
                .setContentText(message)
                .setSmallIcon(R.drawable.notification)
                .setTicker(ticket)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();
        sNotificationManager.notify(id, notification);
    }

    public static void cancelNotification(final int id) {
        sNotificationManager.cancel(id);
    }
}
