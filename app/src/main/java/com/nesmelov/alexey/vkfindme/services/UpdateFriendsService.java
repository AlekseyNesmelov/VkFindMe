package com.nesmelov.alexey.vkfindme.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.ui.activities.MainActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.models.LatLonUserModel;
import com.nesmelov.alexey.vkfindme.models.LatLonUsersModel;
import com.nesmelov.alexey.vkfindme.models.UserModel;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.structures.Alarm;
import com.nesmelov.alexey.vkfindme.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        if (!mStorage.getRefreshFriends()) {
            FindMeApp.showToast(this, getString(R.string.refresh_friends_is_off));
        }
        super.onDestroy();
    }

    public void onUpdate(Response<LatLonUsersModel> response) {
        try {
            final List<Integer> allUsers = mStorage.getUserIds();

            final Map<Integer, List<Alarm>> alarmUsers = mStorage.getAlarmUsers();

            for (final LatLonUserModel latLonUserModel : response.body().getUsers()) {
                final Integer id = latLonUserModel.getUser();
                final double lat = latLonUserModel.getLat();
                final double lon  = latLonUserModel.getLon();
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
        } catch (Exception e) {
        }
        refreshData();
    }

    private void startRefreshing() {
        if (mStorage.getRefreshFriends() && (mStorage.isUserUpdateListenerExist() || mStorage.isAlarmExist())) {
            final List<UserModel> userModels = mStorage.getUserModels();
            mHTTPManager.getUserPositions(userModels,
                    new Callback<LatLonUsersModel>() {
                        @Override
                        public void onResponse(Call<LatLonUsersModel> call, Response<LatLonUsersModel> response) {
                            if (response.body() != null) {
                                onUpdate(response);
                            }
                        }

                        @Override
                        public void onFailure(Call<LatLonUsersModel> call, Throwable t) {
                            refreshData();
                        }
                    });
        } else {
            stopSelf();
        }
    }

    private void refreshData() {
        if (mStorage.getRefreshFriends() && (mStorage.isUserUpdateListenerExist() || mStorage.isAlarmExist())) {
            mHandler.postDelayed(() -> {
                final List<UserModel> userModels = mStorage.getUserModels();
                mHTTPManager.getUserPositions(userModels,
                        new Callback<LatLonUsersModel>() {
                            @Override
                            public void onResponse(Call<LatLonUsersModel> call, Response<LatLonUsersModel> response) {
                                if (response.body() != null) {
                                    onUpdate(response);
                                }
                            }

                            @Override
                            public void onFailure(Call<LatLonUsersModel> call, Throwable t) {
                                refreshData();
                            }
                        });
            }, mStorage.getRefreshFriendsDelay());
        } else {
            stopSelf();
        }
    }
}



