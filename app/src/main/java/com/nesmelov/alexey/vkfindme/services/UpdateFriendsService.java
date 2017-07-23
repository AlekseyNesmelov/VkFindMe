package com.nesmelov.alexey.vkfindme.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.activities.MainActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.utils.Utils;
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
        if (mStorage.getRefreshFriends() && (mStorage.isUserUpdateListenerExist() || mStorage.isAlarmExist())) {
            FindMeApp.displayActiveNotification(FRIENDS_REFRESH_NOTIFICATION_ID, this, getString(R.string.app_name),
                    getString(R.string.refresh_friends_is_on), MainActivity.class);
            mHandler.removeCallbacksAndMessages(null);
            startRefreshing();
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        FindMeApp.cancelNotification(FRIENDS_REFRESH_NOTIFICATION_ID);
        FindMeApp.showToast(this, getString(R.string.refresh_friends_is_off));
        super.onDestroy();
    }

    @Override
    public void onUpdate(int request, JSONObject update) {
        try {
            final List<Integer> allUsers = mStorage.getUserIds();

            final Map<Integer, List<Alarm>> alarmUsers = mStorage.getAlarmUsers();

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
                        if (Utils.checkAlarm(alarm, lat, lon)) {
                            mStorage.removeAlarmParticipant(alarm.getAlarmId(), id);
                            updatedAlarms.add(alarm);
                            break;
                        }
                    }
                }

                if (!updatedAlarms.isEmpty()) {
                    final String userName = mStorage.getUserName(id);
                    final StringBuilder message = new StringBuilder(getString(R.string.friend_in_alarm));
                    if (!userName.isEmpty()) {
                        message.append(": ").append(userName);
                    }
                    FindMeApp.displayAlarmRingNotification(id, this, getString(R.string.app_name),
                            message.toString(), MainActivity.class,
                            updatedAlarms.get(0).getLat(),
                            updatedAlarms.get(0).getLon());
                    for (final Alarm alarm : updatedAlarms) {
                        if (mStorage.isAlarmCompleted(alarm.getAlarmId())) {
                            mStorage.removeAlarm(alarm.getAlarmId());
                        }
                    }
                    if (!(mStorage.getRefreshFriends() && (mStorage.isUserUpdateListenerExist() || mStorage.isAlarmExist()))) {
                        stopSelf();
                    }
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

    private void startRefreshing() {
        if (mStorage.getRefreshFriends() && (mStorage.isUserUpdateListenerExist() || mStorage.isAlarmExist())) {
            mHTTPManager.executeRequest(HTTPManager.REQUEST_GET_USERS_POS,
                    HTTPManager.REQUEST_GET_USERS_POS, UpdateFriendsService.this, mStorage.getUserIdsString());
        } else {
            stopSelf();
        }
    }

    private void refreshData() {
        if (mStorage.getRefreshFriends() && (mStorage.isUserUpdateListenerExist() || mStorage.isAlarmExist())) {
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



