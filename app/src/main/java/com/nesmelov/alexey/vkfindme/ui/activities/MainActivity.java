package com.nesmelov.alexey.vkfindme.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.models.StatusModel;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.VKManager;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.structures.User;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Main activity that needs to login user.
 */
public class MainActivity extends Activity {
    private static final int LOCATION_REQUEST_CODE = 0;

    private Storage mStorage;
    private HTTPManager mHTTPManager;
    private VKManager mVKManager;
    private VKRequest.VKRequestListener mUserInfoRequestListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        mStorage = FindMeApp.getStorage();
        mVKManager = FindMeApp.getVKManager();
        mHTTPManager = FindMeApp.getHTTPManager();

        mUserInfoRequestListener = new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(final VKResponse response) {
                try {
                    final JSONArray jsonResponse = response.json.getJSONArray(VKManager.RESPONSE);
                    final JSONObject jsonObjectRequest = jsonResponse.getJSONObject(0);

                    final User user = new User();

                    final Integer id = jsonObjectRequest.getInt(VKManager.ID);//48327366;
                    mStorage.setUserVkId(id);
                    user.setVkId(id);

                    final String photoUrl = jsonObjectRequest.getString(VKManager.PHOTO_MAX);
                    mStorage.setUserIconUrl(photoUrl);
                    user.setIconUrl(photoUrl);

                    final String firstName = jsonObjectRequest.getString(VKManager.FIRST_NAME);
                    mStorage.setUserName(firstName);
                    user.setName(firstName);

                    final String lastName = jsonObjectRequest.getString(VKManager.LAST_NAME);
                    mStorage.setUserSurname(lastName);
                    user.setSurname(lastName);

                    mStorage.addUser(user);

                    mHTTPManager.addUser(user.getVkId(), new Callback<StatusModel>() {
                        @Override
                        public void onResponse(@NonNull Call<StatusModel> call, @NonNull Response<StatusModel> response) {
                            final StatusModel statusModel = response.body();
                            if (response.isSuccessful() && statusModel != null
                                    && (statusModel.getStatus().equals(StatusModel.OK)
                                    || statusModel.getStatus().equals(StatusModel.ALREADY_EXISTS))) {
                                MainActivity.this.onSuccess();
                            } else {
                                MainActivity.this.onError();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<StatusModel> call, @NonNull Throwable t) {
                            MainActivity.this.onError();
                        }
                    });
                } catch (Exception e) {
                    Log.d("ANESMELOV", e.toString());
                    MainActivity.this.onError();
                }
            }

            @Override
            public void onError(final VKError error) {
                MainActivity.this.onError();
            }

            @Override
            public void attemptFailed(final VKRequest request, final int attemptNumber, final int totalAttempts) {
                MainActivity.this.onError();
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
                mVKManager.getUserInfo(mUserInfoRequestListener);
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
                                           @NonNull final String permissions[], @NonNull final int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    login();
                } else {
                    FindMeApp.showToast(MainActivity.this, getString(R.string.location_not_granted));
                    finish();
                }
                break;
            }
        }
    }

    /**
     * Success authorization event.
     */
    public void onSuccess() {
        final Intent intent = new Intent(this, TabHostActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Failure authorization event.
     */
    public void onError() {
        FindMeApp.showPopUp(this, getString(R.string.error_title), getString(R.string.server_is_not_accessible),
                (dialog, which) -> finish());
    }

    /**
     * VK login.
     */
    private void login() {
        if (VKSdk.wakeUpSession(this)) {
            mVKManager.getUserInfo(mUserInfoRequestListener);
        } else {
            VKSdk.login(this, VKScope.FRIENDS, VKScope.PHOTOS);
        }
    }
}
