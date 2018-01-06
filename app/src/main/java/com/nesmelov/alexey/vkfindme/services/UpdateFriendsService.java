package com.nesmelov.alexey.vkfindme.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.ui.activities.MainActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.models.LatLonUserModel;
import com.nesmelov.alexey.vkfindme.network.models.LatLonUsersModel;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.ui.markers.AlarmMarker;
import com.nesmelov.alexey.vkfindme.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Service that updates friends coordinates.
 */
public class UpdateFriendsService extends Service {
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

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (mStorage.getRefreshFriends() && (mStorage.isUserUpdatedListenerExist() || mStorage.isAlarmForFriendExist())) {
            FindMeApp.displayActiveNotification(FRIENDS_REFRESH_NOTIFICATION_ID, this, getString(R.string.app_name),
                    getString(R.string.refresh_friends_is_on), MainActivity.class);
            mHandler.removeCallbacksAndMessages(null);
            startRefreshing();
        } else {
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        FindMeApp.cancelNotification(FRIENDS_REFRESH_NOTIFICATION_ID);
        super.onDestroy();
    }

    /**
     * Friends update event.
     *
     * @param response response to handle update.
     */
    public void onUpdate(Response<LatLonUsersModel> response) {
        try {
            final List<Integer> allUsers = mStorage.getUserIds();

            final LinkedHashMap<Integer, List<AlarmMarker>> alarmUsers = mStorage.getAlarmUsers();

            for (final LatLonUserModel latLonUserModel : response.body().getUsers()) {
                final Integer id = latLonUserModel.getUser();
                final double lat = latLonUserModel.getLat();
                final double lon  = latLonUserModel.getLon();
                allUsers.remove(id);
                mStorage.setUserPos(id, lat, lon);

                final List<AlarmMarker> updatedAlarms = new ArrayList<>();
                final List<AlarmMarker> alarms = alarmUsers.get(id);
                if (alarms != null) {
                    for (final AlarmMarker alarm : alarms) {
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
                    for (final AlarmMarker alarm : updatedAlarms) {
                        if (mStorage.isAlarmCompleted(alarm.getAlarmId())) {
                            mStorage.removeAlarm(alarm.getAlarmId());
                        }
                    }
                    if (!(mStorage.getRefreshFriends() && (mStorage.isUserUpdatedListenerExist() || mStorage.isAlarmForFriendExist()))) {
                        stopSelf();
                    }
                }
            }

            for (final Integer invisibleUser : allUsers) {
                mStorage.makeUserInvisible(invisibleUser);
            }
        } catch (Exception e) {
            Log.d(FindMeApp.TAG, "UpdateFriendsService", e);
        }
        refreshData();
    }

    /**
     * Starts refreshing of friends positions.
     */
    private void startRefreshing() {
        final List<Integer> userModels = mStorage.getUserIds();
        mHTTPManager.getUserPositions(userModels,
                new Callback<LatLonUsersModel>() {
                    @Override
                    public void onResponse(@NonNull Call<LatLonUsersModel> call, @NonNull Response<LatLonUsersModel> response) {
                        if (response.body() != null) {
                            onUpdate(response);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LatLonUsersModel> call, @NonNull Throwable t) {
                        refreshData();
                    }
                });
    }

    /**
     * Refreshes friends data.
     */
    private void refreshData() {
        if (mStorage.getRefreshFriends() && (mStorage.isUserUpdatedListenerExist() || mStorage.isAlarmForFriendExist())) {
            mHandler.postDelayed(() -> {
                final List<Integer> userModels = mStorage.getUserIds();
                mHTTPManager.getUserPositions(userModels,
                        new Callback<LatLonUsersModel>() {
                            @Override
                            public void onResponse(@NonNull Call<LatLonUsersModel> call, @NonNull Response<LatLonUsersModel> response) {
                                if (response.body() != null) {
                                    onUpdate(response);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<LatLonUsersModel> call, @NonNull Throwable t) {
                                refreshData();
                            }
                        });
            }, mStorage.getRefreshFriendsDelay());
        } else {
            stopSelf();
        }
    }
}



