package com.nesmelov.alexey.vkfindme.network;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nesmelov.alexey.vkfindme.models.FullUserInfoModel;
import com.nesmelov.alexey.vkfindme.models.LatLonUserModel;
import com.nesmelov.alexey.vkfindme.models.LatLonUsersModel;
import com.nesmelov.alexey.vkfindme.models.StatusModel;
import com.nesmelov.alexey.vkfindme.models.UserModel;
import com.nesmelov.alexey.vkfindme.models.UsersModel;
import com.nesmelov.alexey.vkfindme.models.VisibilityUserModel;

import java.text.DateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HTTPManager {

    private static final String SERVER_URL = "http://fmapp-fm-app.a3c1.starter-us-west-1.openshiftapps.com/findme-app/";

    private FindMeApi mFindMeApi;

    public HTTPManager() {
        final Gson gson = new GsonBuilder()
                .serializeNulls()
                .setDateFormat(DateFormat.LONG)
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .create();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(6, TimeUnit.SECONDS)
                .connectTimeout(6, TimeUnit.SECONDS)
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
        mFindMeApi = retrofit.create(FindMeApi.class);
    }

    public void addUser(final Integer user, final Callback<StatusModel> callback) {
        final UserModel userModel = new UserModel(user);
        mFindMeApi.postAddUser(userModel).enqueue(callback);
    }

    public void showMe(final Integer user, final Double lat, final Double lon, final Callback<StatusModel> callback) {
        final FullUserInfoModel fullUserInfoModel = new FullUserInfoModel(user, lat, lon, true);
        mFindMeApi.postSetVisible(fullUserInfoModel).enqueue(callback);
    }

    public void hideMe(final Integer user, final Callback<StatusModel> callback) {
        final VisibilityUserModel visibilityUserModel = new VisibilityUserModel(user,  false);
        mFindMeApi.postSetInvisible(visibilityUserModel).enqueue(callback);
    }

    public void sendPosition(final Integer user, final Double lat, final Double lon, final Callback<StatusModel> callback) {
        final LatLonUserModel latLonUserModel = new LatLonUserModel(user,  lat, lon);
        mFindMeApi.postSetPosition(latLonUserModel).enqueue(callback);
    }

    public void checkUsers(final List<UserModel> users, final Callback<UsersModel> callback) {
        final UsersModel usersModel = new UsersModel(users);
        mFindMeApi.postCheckUsers(usersModel).enqueue(callback);
    }

    public void getUserPositions(final List<UserModel> users, final Callback<LatLonUsersModel> callback) {
        final UsersModel usersModel = new UsersModel(users);
        mFindMeApi.postGetPositions(usersModel).enqueue(callback);
    }

    public void loadImage(final String url, final okhttp3.Callback callback) {
        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
