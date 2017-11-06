package com.nesmelov.alexey.vkfindme.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.ui.activities.MainActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.models.StatusModel;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * GPS service that checks the user in alarms and sends position if
 * visibility is turned on.
 */
public class GpsService extends Service implements LocationListener {
    public static final int VISIBLE_NOTIFICATION_ID = 111;
    public static final int ME_IN_ALARM_NOTIFICATION_ID = 222;
    public static final int RING_ALARM_NOTIFICATION_ID = 333;

    private LocationManager mLocationManager;
    private Storage mStorage;
    private HTTPManager mHTTPManager;

    private final Object mLock = new Object();
    private List<Alarm> mAlarmsForMe = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStorage = FindMeApp.getStorage();
        mHTTPManager = FindMeApp.getHTTPManager();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (isVisible() || isMeInAlarm()) {

            synchronized (mLock) {
                mAlarmsForMe = mStorage.getAlarmsForMe();
            }

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                onLocationChanged(mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        mStorage.getGPSMinDelay(), mStorage.getGPSMinDistance(), this);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        mStorage.getGPSMinDelay(), mStorage.getGPSMinDistance(), this);

                if (isVisible()) {
                    FindMeApp.displayActiveNotification(VISIBLE_NOTIFICATION_ID, this, getString(R.string.app_name),
                            getString(R.string.gps_service_on_message), MainActivity.class);
                } else {
                    FindMeApp.cancelNotification(VISIBLE_NOTIFICATION_ID);
                }
                if (isMeInAlarm()) {
                    FindMeApp.displayActiveNotification(ME_IN_ALARM_NOTIFICATION_ID, this, getString(R.string.app_name),
                            getString(R.string.me_in_alarm), MainActivity.class);
                } else {
                    FindMeApp.cancelNotification(ME_IN_ALARM_NOTIFICATION_ID);
                }
            }
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                mLocationManager != null) {
            mLocationManager.removeUpdates(this);
            mLocationManager = null;
        }
        FindMeApp.cancelNotification(VISIBLE_NOTIFICATION_ID);
        FindMeApp.cancelNotification(ME_IN_ALARM_NOTIFICATION_ID);
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (location != null) {

            mStorage.setUserLat(location.getLatitude());
            mStorage.setUserLon(location.getLongitude());

            if (mHTTPManager != null && isVisible()) {
                mHTTPManager.sendPosition(mStorage.getUserVkId(), location.getLatitude(), location.getLongitude(),
                        new Callback<StatusModel>() {
                            @Override
                            public void onResponse(@NonNull Call<StatusModel> call, @NonNull Response<StatusModel> response) {
                            }

                            @Override
                            public void onFailure(@NonNull Call<StatusModel> call, @NonNull Throwable t) {
                            }
                        });
            }

            synchronized (mLock) {
                Alarm updatedAlarm = null;
                for (final Alarm alarm : mAlarmsForMe) {
                    if (Utils.checkAlarm(alarm, location.getLatitude(), location.getLongitude())) {
                        mStorage.removeAlarmParticipant(alarm.getAlarmId(), mStorage.getUserVkId());
                        updatedAlarm = alarm;
                        break;
                    }
                }
                if (updatedAlarm != null) {
                    FindMeApp.displayAlarmRingNotification(RING_ALARM_NOTIFICATION_ID, this, getString(R.string.app_name),
                            getString(R.string.reach_alarm), MainActivity.class, updatedAlarm.getLat(), updatedAlarm.getLon());
                    mAlarmsForMe.remove(updatedAlarm);
                    if (mStorage.isAlarmCompleted(updatedAlarm.getAlarmId())) {
                        mStorage.removeAlarm(updatedAlarm.getAlarmId());
                    }
                    if (!isVisible() && !isMeInAlarm()) {
                        stopSelf();
                    }
                }
            }
        }
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
    }

    @Override
    public void onProviderEnabled(final String provider) {
    }

    @Override
    public void onProviderDisabled(final String provider) {
    }

    /**
     * Returns <tt>true</tt> if user is visible.
     *
     * @return <tt>true</tt> if user is visible.
     */
    private boolean isVisible() {
        return mStorage.getVisibility();
    }

    /**
     * Returns <tt>true</tt> if user is in alarm.
     *
     * @return <tt>true</tt> if user is in alarm.
     */
    private boolean isMeInAlarm() {
        return mStorage.isMeInAlarm();
    }
}



