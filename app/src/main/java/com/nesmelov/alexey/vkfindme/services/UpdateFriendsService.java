package com.nesmelov.alexey.vkfindme.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.activities.MainActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import com.nesmelov.alexey.vkfindme.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UpdateFriendsService extends Service implements OnUpdateListener{
    public static final int FRIENDS_REFRESH_NOTIFICATION_ID = 444;

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
        FindMeApp.displayNotification(FRIENDS_REFRESH_NOTIFICATION_ID, this, getString(R.string.app_name),
                getString(R.string.refresh_friends_is_on), MainActivity.class);
    }

    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d("UpdateFriendsService", "onStartCommand");
        if (mStorage.gerRefreshFriends()) {
            mHandler.removeCallbacksAndMessages(null);
            refreshData();
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.d("UpdateFriendsService", "onDestroy");
        FindMeApp.cancelNotification(FRIENDS_REFRESH_NOTIFICATION_ID);
        super.onDestroy();
    }

    @Override
    public void onUpdate(int request, JSONObject update) {
        try {
            final List<Integer> allUsers = mStorage.getUserIds();

            final JSONArray users = update.getJSONArray("users");
            for (int i = 0; i < users.length(); i++) {
                final JSONObject user = users.getJSONObject(i);
                final Integer id = user.getInt("user");
                final double lat = user.getDouble("lat");
                final double lon  = user.getDouble("lon");
                allUsers.remove(id);
                mStorage.setUserPos(id, lat, lon);
            }

            for (final Integer invisibleUser : allUsers) {
                mStorage.makeUserInvisible(invisibleUser);
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
}



