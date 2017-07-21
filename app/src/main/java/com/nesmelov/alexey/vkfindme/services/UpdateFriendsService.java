package com.nesmelov.alexey.vkfindme.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
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
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.structures.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        if (mStorage.getRefreshFriends()) {
            FindMeApp.displayActiveNotification(FRIENDS_REFRESH_NOTIFICATION_ID, this, getString(R.string.app_name),
                    getString(R.string.refresh_friends_is_on), MainActivity.class);
        }
    }

    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d("UpdateFriendsService", "onStartCommand");
        mHandler.removeCallbacksAndMessages(null);
        refreshData();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.d("UpdateFriendsService", "onDestroy");
        FindMeApp.cancelNotification(FRIENDS_REFRESH_NOTIFICATION_ID);
        FindMeApp.showToast(this, getString(R.string.refresh_friends_is_off));
        super.onDestroy();
    }

    @Override
    public void onUpdate(int request, JSONObject update) {
        try {
            final List<Integer> allUsers = mStorage.getUserIds();

            final Map<User, List<Alarm>> alarmUsers = mStorage.getAlarmUsers();

            final JSONArray users = update.getJSONArray("users");
            for (int i = 0; i < users.length(); i++) {
                final JSONObject user = users.getJSONObject(i);
                final Integer id = user.getInt("user");
                final double lat = user.getDouble("lat");
                final double lon  = user.getDouble("lon");
                allUsers.remove(id);
                mStorage.setUserPos(id, lat, lon);

                final List<Alarm> updatedAlarms = new ArrayList<>();
                final List<Alarm> alarms = alarmUsers.get(id);
                if (alarms != null) {
                    for (final Alarm alarm : alarms) {
                        float results[] = new float[1];
                        Location.distanceBetween(
                                alarm.getLat(),
                                alarm.getLon(),
                                lat,
                                lon,
                                results);
                        if (results[0] < alarm.getRadius()) {
                            mStorage.removeAlarmParticipant(alarm.getAlarmId(), id);
                            updatedAlarms.add(alarm);
                            break;
                        }
                    }
                }

                if (!updatedAlarms.isEmpty()) {
                    /*FindMeApp.displayAlarmRingNotification(id, this, getString(R.string.app_name),
                            user., MainActivity.class);
                    mAlarmsForMe.remove(updatedAlarm);
                    if (mStorage.isAlarmCompleted(updatedAlarm.getAlarmId())) {
                        mStorage.removeAlarm(updatedAlarm.getAlarmId());
                    }
                    if (!isVisible() && !isMeInAlarm()) {
                        stopSelf();
                    }*/
                }
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
        if (mStorage.getRefreshFriends()/* && mStorage.isUserUpdateListenerExist()*/) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mHTTPManager.executeRequest(HTTPManager.REQUEST_GET_USERS_POS,
                            HTTPManager.REQUEST_GET_USERS_POS, UpdateFriendsService.this,
                            mStorage.getUserIdsString());
                }
            }, mStorage.getRefreshFriendsDelay());
        } else {
            stopSelf();
        }
    }
}



