package com.nesmelov.alexey.vkfindme.utils;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.DisplayMetrics;
import com.nesmelov.alexey.vkfindme.ui.markers.AlarmMarker;

import java.util.Random;

/**
 * Utilities class.
 */
public class Utils {

    private static Random sRandom = new Random();

    /**
     * Converts dp to pixels.
     *
     * @param context context to use.
     * @param dp input dp.
     * @return pixels.
     */
    public static int dpToPx(final Context context, final int dp) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Returns <tt>true</tt> if alarm is completed by distance.
     *
     * @param alarm alarm to check.
     * @param lat latitude.
     * @param lon longitude.
     * @return <tt>true</tt> if alarm is completed.
     */
    public static boolean checkAlarm(final AlarmMarker alarm, final double lat, final double lon) {
        final float results[] = new float[1];
        Location.distanceBetween(alarm.getLat(), alarm.getLon(), lat, lon, results);
        return results[0] < alarm.getRadius();
    }

    /**
     * Returns random color.
     *
     * @return random color.
     */
    public static int getRandomColor() {
        return Color.argb(96,
                sRandom.nextInt(255),
                sRandom.nextInt(255),
                sRandom.nextInt(255));
    }
}
