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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.activities.MainActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.utils.Utils;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class GpsService extends Service implements LocationListener, OnUpdateListener{
    public static final int VISIBLE_NOTIFICATION_ID = 111;
    public static final int ME_IN_ALARM_NOTIFICATION_ID = 222;
    public static final int RING_ALARM_NOTIFICATION_ID = 333;

    private LocationManager mLocationManager;
    private Storage mStorage;
    private HTTPManager mHTTPManager;

    private Object mLock = new Object();
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
    }

    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (isVisible() || isMeInAlarm()) {

            synchronized (mLock) {
                mAlarmsForMe = mStorage.getAlarmsForMe();
            }

            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            onLocationChanged(mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                mHTTPManager.executeRequest(HTTPManager.REQUEST_SET_POSITION, HTTPManager.REQUEST_IDLE, this,
                        String.valueOf(mStorage.getUserVkId()),
                        String.valueOf(location.getLatitude()),
                        String.valueOf(location.getLongitude()));
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
                            getString(R.string.reach_alarm), MainActivity.class);
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

    @Override
    public void onUpdate(final int request, final JSONObject update) {
    }

    @Override
    public void onError(final int request, final int errorCode) {
    }

    private boolean isVisible() {
        return mStorage.getVisibility();
    }

    private boolean isMeInAlarm() {
        return mStorage.isMeInAlarm();
    }
}



