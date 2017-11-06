package com.nesmelov.alexey.vkfindme.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;
import android.util.DisplayMetrics;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import java.util.Random;

/**
 * Utilities class.
 */
public class Utils {

    private static Random sRandom = new Random();

    /**
     * Gets circle cropped bitmap.
     *
     * @param bitmap original bitmap.
     * @return circle cropped bitmap.
     */
    public static Bitmap getCroppedBitmap(final Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * Converts dp to pixels.
     *
     * @param context context to use.
     * @param dp input dp.
     * @return pixels.
     */
    public static int dpToPx(final Context context, final int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
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
    public static boolean checkAlarm(final Alarm alarm, final double lat, final double lon) {
        float results[] = new float[1];
        Location.distanceBetween(alarm.getLat(), alarm.getLon(), lat, lon, results);
        return results[0] < alarm.getRadius();
    }

    /**
     * Returns random color.
     *
     * @return
     */
    public static int getRandomColor() {
        return Color.argb(96,
                sRandom.nextInt(255),
                sRandom.nextInt(255),
                sRandom.nextInt(255));
    }
}
