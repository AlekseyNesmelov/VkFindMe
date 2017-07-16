package com.nesmelov.alexey.vkfindme.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
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
import com.nesmelov.alexey.vkfindme.structures.Alarm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UpdateFriendsService extends Service implements OnUpdateListener{
    private Storage mStorage;
    private HTTPManager mHTTPManager;

    final Handler mHandler = new Handler();

    private List<Long> mNotifications = new CopyOnWriteArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("UpdateFriendsService", "onCreate");
        super.onCreate();
        mStorage = FindMeApp.getStorage();
        mHTTPManager = FindMeApp.getHTTPManager();
    }

    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d("UpdateFriendsService", "onStartCommand");
        mHandler.removeCallbacksAndMessages(null);
        refreshData();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.d("UpdateFriendsService", "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onUpdate(int request, JSONObject update) {
        try {
            final JSONArray users = update.getJSONArray("users");
            for (int i = 0; i < users.length(); i++) {
                final JSONObject user = users.getJSONObject(i);
                final int id = user.getInt("user");
                final double lat = user.getDouble("lat");
                final double lon  = user.getDouble("lon");
                mStorage.setUserPos(id, lat, lon);
            }
        } catch (JSONException e) {
        }
        refreshData();
    }

    @Override
    public void onError(int request, int errorCode) {
        refreshData();
    }

    private void refreshData() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHTTPManager.executeRequest(HTTPManager.REQUEST_GET_USERS_POS,
                        HTTPManager.REQUEST_GET_USERS_POS, UpdateFriendsService.this,
                        mStorage.getUserIdsString());
            }
        }, mStorage.getRefreshFriendsDelay());
    }

    private boolean isVisible() {
        return mStorage.getVisibility();
    }

    private boolean isMeInAlarm() {
        return mStorage.isMeInAlarm();
    }
}



