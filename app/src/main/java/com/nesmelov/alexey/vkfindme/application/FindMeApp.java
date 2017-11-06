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
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.vk.sdk.VKSdk;

/**
 * Find Me application class.
 */
public class FindMeApp extends Application {
    public static final String USERS_DATABASE_NAME = "USERS_DATABASE";

    private static HTTPManager sHTTPManager;
    private static Storage sStorage;
    private static NotificationManager sNotificationManager;
    private static VKManager sVKManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sHTTPManager = new HTTPManager();
        sStorage = new Storage(this);
        sNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sVKManager = new VKManager();
        VKSdk.initialize(this);
    }

    /**
     * Returns VK manager.
     *
     * @return VK manager.
     */
    public static VKManager getVKManager() {
        return sVKManager;
    }

    /**
     * Returns HTTP manager.
     *
     * @return HTTP manager.
     */
    public static HTTPManager getHTTPManager() {
        return sHTTPManager;
    }

    /**
     * Returns application storage.
     *
     * @return application storage.
     */
    public static Storage getStorage() {
        return sStorage;
    }

    /**
     * Shows simple pop up.
     *
     * @param context context to use.
     * @param title pop up title.
     * @param message pop up message.
     */
    public static void showPopUp(final Context context, final String title, final String message) {
        final AlertDialog adb = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                })
                .create();
        adb.show();
    }

    /**
     * Shows simple pop up with onclick listener.
     *
     * @param context context to use.
     * @param title pop up title.
     * @param message pop up message.
     * @param listener onclick listener.
     */
    public static void showPopUp(final Context context, final String title, final String message,
                                 final DialogInterface.OnClickListener listener) {
        final AlertDialog adb = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, listener)
                .create();
        adb.show();
    }

    /**
     * Shows a toast.
     *
     * @param context context to use.
     * @param message toast message.
     */
    public static void showToast(final Context context, final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays an active action notification.
     *
     * @param id notification id.
     * @param context context to use.
     * @param ticket notification ticket.
     * @param message notification message.
     * @param pendingClass notification pending class.
     */
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

    /**
     * Displays an alarm notification.
     *
     * @param id notification id.
     * @param context context to use.
     * @param ticket notification ticket.
     * @param message notification message.
     * @param pendingClass notification pending class.
     * @param lat alarm latitude.
     * @param lon alarm longitude.
     */
    public static void displayAlarmRingNotification(final int id, final Context context,
                                           final String ticket, final String message, final Class<?> pendingClass,
                                                    final double lat, final double lon) {
        final Intent intent = new Intent(context, pendingClass);
        intent.putExtra(Storage.LAT, String.valueOf(lat));
        intent.putExtra(Storage.LON, String.valueOf(lon));
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

    /**
     * Cancels a notification.
     *
     * @param id id of notification to cancel.
     */
    public static void cancelNotification(final int id) {
        sNotificationManager.cancel(id);
    }
}
