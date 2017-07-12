package com.nesmelov.alexey.vkfindme.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import com.nesmelov.alexey.vkfindme.network.VKManager;
import com.nesmelov.alexey.vkfindme.pages.ProfileFragment;
import com.nesmelov.alexey.vkfindme.storage.Const;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity implements OnUpdateListener{
    private static final int LOCATION_REQUEST_CODE = 0;

    private Storage mStorage;
    private HTTPManager mHTTPManager;
    private VKManager mVKManager;
    private VKRequest.VKRequestListener mUserInfoRequestListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStorage = FindMeApp.getStorage();
        mVKManager = FindMeApp.getVKManager();
        mHTTPManager = FindMeApp.getHTTPManager();

        mUserInfoRequestListener = new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(final VKResponse response) {
                try {
                    final JSONArray jsonResponse = response.json.getJSONArray(VKManager.RESPONSE);
                    final JSONObject jsonObjectRequest = jsonResponse.getJSONObject(0);

                    final Integer id = jsonObjectRequest.getInt(VKManager.ID);
                    mStorage.setUserVkId(id);

                    final String photoUrl = jsonObjectRequest.getString(VKManager.PHOTO_MAX);
                    mStorage.setUserIconUrl(photoUrl);

                    final String firstName = jsonObjectRequest.getString(VKManager.FIRST_NAME);
                    mStorage.setUserName(firstName);

                    final String lastName = jsonObjectRequest.getString(VKManager.LAST_NAME);
                    mStorage.setUserSurname(lastName);

                    mStorage.addUser(mStorage.getUserVkId(), mStorage.getUserName(), mStorage.getUserSurname(),
                            Const.BAD_LAT, Const.BAD_LON, mStorage.getUserIconUrl());

                    mHTTPManager.executeRequest(HTTPManager.REQUEST_ADD_USER,
                            HTTPManager.REQUEST_IDLE,
                            MainActivity.this, mStorage.getUserVkId().toString());

                } catch (Exception e) {
                    MainActivity.this.onError(HTTPManager.REQUEST_ADD_USER, HTTPManager.SERVER_ERROR_CODE);
                }
            }

            @Override
            public void onError(final VKError error) {
                MainActivity.this.onError(HTTPManager.REQUEST_ADD_USER, HTTPManager.SERVER_ERROR_CODE);
            }

            @Override
            public void attemptFailed(final VKRequest request, final int attemptNumber, final int totalAttempts) {
                MainActivity.this.onError(HTTPManager.REQUEST_ADD_USER, HTTPManager.SERVER_ERROR_CODE);
            }
        };

        if ( Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION )
                == PackageManager.PERMISSION_GRANTED) {
            login();
        } else {
            ActivityCompat.requestPermissions(
                    MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(final VKAccessToken res) {
                mVKManager.executeRequest(VKManager.REQUEST_GET_USER_INFO, mUserInfoRequestListener);
            }
            @Override
            public void onError(final VKError error) {
                FindMeApp.showToast(MainActivity.this, getString(R.string.needs_authorization));
                finish();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           final String permissions[], final int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    login();
                } else {
                    finish();
                }
                break;
            }
        }
    }

    private void start() {
        final Intent intent = new Intent(this, TabHostActivity.class);
        startActivity(intent);
        finish();
    }

    private void login() {
        if (VKSdk.wakeUpSession(this)) {
            mVKManager.executeRequest(VKManager.REQUEST_GET_USER_INFO, mUserInfoRequestListener);
        } else {
            VKSdk.login(this, VKScope.FRIENDS, VKScope.PHOTOS);
        }
    }

    @Override
    public void onUpdate(final int request, final JSONObject update) {
        start();
    }

    @Override
    public void onError(final int request, final int errorCode) {
        FindMeApp.showPopUp(this, getString(R.string.error_title), getString(R.string.server_is_not_accessible),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        finish();
                    }
                });
    }
}
