package com.nesmelov.alexey.vkfindme.network;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.models.FullUserInfoModel;
import com.nesmelov.alexey.vkfindme.network.models.LatLonUserModel;
import com.nesmelov.alexey.vkfindme.network.models.LatLonUsersModel;
import com.nesmelov.alexey.vkfindme.network.models.StatusModel;
import com.nesmelov.alexey.vkfindme.network.models.UserModel;
import com.nesmelov.alexey.vkfindme.network.models.UsersModel;
import com.nesmelov.alexey.vkfindme.network.models.VisibilityUserModel;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * HTTP server that connects to the server.
 */
public class HTTPManager {

    private static final String SERVER_URL = "http://anesmelov-anesmelov-app.a3c1.starter-us-west-1.openshiftapps.com/findme-app/";
    private static final int TIMEOUT = 10;

    private FindMeApi mFindMeApi;

    /**
     * Constructs HTTP manager.
     */
    public HTTPManager() {
        final Gson gson = new GsonBuilder()
                .serializeNulls()
                .setDateFormat(DateFormat.LONG)
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .create();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
        mFindMeApi = retrofit.create(FindMeApi.class);
    }

    /**
     * Adds user to server database.
     *
     * @param user user id to add.
     * @param callback response callback.
     */
    public void addUser(final Integer user, final Callback<StatusModel> callback) {
        final UserModel userModel = new UserModel(user);
        mFindMeApi.postAddUser(userModel).enqueue(callback);
    }

    /**
     * Turns on visibility.
     *
     * @param user user id.
     * @param lat user latitude.
     * @param lon user longitude.
     * @param callback response callback.
     */
    public void showMe(final Integer user, final Double lat, final Double lon, final Callback<StatusModel> callback) {
        final FullUserInfoModel fullUserInfoModel = new FullUserInfoModel(user, lat, lon, true);
        mFindMeApi.postSetVisible(fullUserInfoModel).enqueue(callback);
    }

    /**
     * Turns off visibility.
     *
     * @param user user id.
     * @param callback response listener.
     */
    public void hideMe(final Integer user, final Callback<StatusModel> callback) {
        final VisibilityUserModel visibilityUserModel = new VisibilityUserModel(user,  false);
        mFindMeApi.postSetInvisible(visibilityUserModel).enqueue(callback);
    }

    /**
     * Sends user position.
     *
     * @param user user id.
     * @param lat user latitude.
     * @param lon user longitude.
     * @param callback response callback.
     */
    public void sendPosition(final Integer user, final Double lat, final Double lon, final Callback<StatusModel> callback) {
        final LatLonUserModel latLonUserModel = new LatLonUserModel(user,  lat, lon);
        mFindMeApi.postSetPosition(latLonUserModel).enqueue(callback);
    }

    /**
     * Checks synchronously if users are in server database.
     *
     * @param users users to check.
     * @return list of users, that are in server database.
     */
    public Response<UsersModel> checkUsersSync(final List<Integer> users) {
        final UsersModel usersModel = new UsersModel(users);
        Response<UsersModel> result = null;
        try {
            result = mFindMeApi.postCheckUsers(usersModel).execute();
        } catch (IOException e) {
            Log.e(FindMeApp.TAG, "HTTPManager:checkUsersSync", e);
        }
        return result;
    }

    /**
     * Gets users positions.
     *
     * @param users users to get positions.
     * @param callback response listener.
     */
    public void getUserPositions(final List<Integer> users, final Callback<LatLonUsersModel> callback) {
        final UsersModel usersModel = new UsersModel(users);
        mFindMeApi.postGetPositions(usersModel).enqueue(callback);
    }
}
