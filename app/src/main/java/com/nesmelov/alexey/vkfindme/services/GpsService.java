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
import android.util.Log;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.activities.MainActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import com.nesmelov.alexey.vkfindme.storage.Storage;

import org.json.JSONObject;

public class GpsService extends Service implements LocationListener, OnUpdateListener{
    public static final int NOTIFICATION_ID = 111;

    private LocationManager mLocationManager;
    private Storage mStorage;
    private HTTPManager mHTTPManager;

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("GpsService", "onCreate");
        super.onCreate();
        mStorage = FindMeApp.getStorage();
        if (shouldStart()) {
            mHTTPManager = FindMeApp.getHTTPManager();
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            onLocationChanged(mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        mStorage.getGPSMinDelay(), mStorage.getGPSMinDistance(), this);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        mStorage.getGPSMinDelay(), mStorage.getGPSMinDistance(), this);

                FindMeApp.displayNotification(NOTIFICATION_ID, this, getString(R.string.app_name),
                        getString(R.string.gps_service_on_message), MainActivity.class);
            }
        } else {
            stopSelf();
        }
    }

    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d("GpsService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.d("GpsService", "onDestroy");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                mLocationManager != null) {
            mLocationManager.removeUpdates(this);
            mLocationManager = null;
        }
        FindMeApp.cancelNotification(NOTIFICATION_ID);
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.d("GpsService", "onLocationChanged");
        if (mHTTPManager != null && location != null) {
            mHTTPManager.executeRequest(HTTPManager.REQUEST_SET_POSITION, HTTPManager.REQUEST_IDLE, this,
                    String.valueOf(mStorage.getUser()),
                    String.valueOf(location.getLatitude()),
                    String.valueOf(location.getLongitude()));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onUpdate(int request, JSONObject update) {

    }

    @Override
    public void onError(int request, int errorCode) {

    }

    private boolean shouldStart() {
        return mStorage.getVisibility();
    }
}



