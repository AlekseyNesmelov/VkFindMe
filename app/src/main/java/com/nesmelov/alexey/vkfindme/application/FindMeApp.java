package com.nesmelov.alexey.vkfindme.application;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.VKManager;
import com.nesmelov.alexey.vkfindme.storage.Const;
import com.nesmelov.alexey.vkfindme.storage.DataBaseHelper;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.vk.sdk.VKSdk;

public class FindMeApp extends Application {
    public static final String USERS_DATABASE_NAME = "USERS_DATABASE";

    private static HTTPManager sHTTPManager;
    private static Storage sStorage;
    private static NotificationManager sNotificationManager;
    private static DataBaseHelper sDataBaseHelper;
    private static VKManager sVKManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sHTTPManager = new HTTPManager();
        sDataBaseHelper = new DataBaseHelper(this, USERS_DATABASE_NAME, null);
        sStorage = new Storage(this);
        sNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sVKManager = new VKManager();
        VKSdk.initialize(this);
    }

    public static VKManager getVKManager() {
        return sVKManager;
    }

    public static HTTPManager getHTTPManager() {
        return sHTTPManager;
    }

    public static Storage getStorage() {
        return sStorage;
    }

    public static DataBaseHelper getDataBaseHelper() {
        return sDataBaseHelper;
    }

    public static void showPopUp(final Context context, final String title, final String message) {
        final AlertDialog adb = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                })
                .create();
        adb.show();
    }

    public static void showPopUp(final Context context, final String title, final String message,
                                 final DialogInterface.OnClickListener listener) {
        final AlertDialog adb = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, listener)
                .create();
        adb.show();
    }

    public static void showToast(final Context context, final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void displayActiveNotification(final int id, final Context context,
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

    public static void displayAlarmRingNotification(final int id, final Context context,
                                           final String ticket, final String message, final Class<?> pendingClass,
                                                    final double lat, final double lon) {
        final Intent intent = new Intent(context, pendingClass);
        intent.putExtra(Const.LAT, String.valueOf(lat));
        intent.putExtra(Const.LON, String.valueOf(lon));
        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        final Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(ticket)
                .setContentText(message)
                .setSmallIcon(R.drawable.notification)
                .setTicker(ticket)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setVibrate(new long[] {1000, 1000, 1000, 1000})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setLights(0xff00ff00, 2000, 4000)
                .build();
        sNotificationManager.notify(id, notification);
    }

    public static void cancelNotification(final int id) {
        sNotificationManager.cancel(id);
    }
}
