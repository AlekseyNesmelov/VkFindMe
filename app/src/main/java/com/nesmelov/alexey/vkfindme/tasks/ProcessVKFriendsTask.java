package com.nesmelov.alexey.vkfindme.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.models.UsersModel;
import com.nesmelov.alexey.vkfindme.ui.markers.UserMarker;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import retrofit2.Response;

/**
 * Task to process VK friends.
 */
public class ProcessVKFriendsTask extends AsyncTask<VKResponse, UserMarker, Void> {

    /**
     * Process VK friends listener.
     */
    public interface OnProcessVKFriendsListener {
        /**
         * VK friend is successfully loaded.
         *
         * @param friend loaded friend.
         */
        void onVKFriendLoaded(final UserMarker friend);

        /**
         * Loading canceled.
         */
        void onVKFriendsLoadingCanceled();

        /**
         * Loading successfully completed.
         */
        void onVKFriendsLoadingCompleted();

        /**
         * Loading failed.
         */
        void onVKFriendsLoadingFailed();
    }

    private LinkedHashMap<Integer, UserMarker> mUsersBuffer = new LinkedHashMap<>();
    private boolean mIsSuccess = true;
    private WeakReference<OnProcessVKFriendsListener> mListener;

    /**
     * Sets loading listener.
     *
     * @param listener listener to set.
     */
    public void setListener(final OnProcessVKFriendsListener listener) {
        mListener = new WeakReference<>(listener);
    }

    @Override
    protected Void doInBackground(VKResponse... params) {
        final VKResponse response = params[0];
        if (!isCancelled()) {
            JSONArray usersArray = null;
            int count = 0;
            try {
                final JSONObject jsonResponse = response.json.getJSONObject("response");
                count = jsonResponse.getInt("count");
                usersArray = jsonResponse.getJSONArray("items");
            } catch (Exception e) {
                Log.e(FindMeApp.TAG, "ProcessVKFriendsTask", e);
                mIsSuccess = false;
            }
            if (mIsSuccess && usersArray != null) {
                final List<Integer> userModels = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    JSONObject userJson = null;
                    Integer id = null;
                    try {
                        userJson = usersArray.getJSONObject(i);
                        id = userJson.getInt("id");
                    } catch (Exception e) {
                        Log.e(FindMeApp.TAG, "ProcessVKFriendsTask", e);
                    }
                    if (userJson != null && id != null) {
                        userModels.add(id);
                        final UserMarker user = new UserMarker();
                        user.setVkId(id);

                        try {
                            user.setName(userJson.getString("first_name"));
                        } catch (Exception e) {
                            Log.e(FindMeApp.TAG, "ProcessVKFriendsTask", e);
                        }

                        try {
                            user.setSurname(userJson.getString("last_name"));
                        } catch (Exception e) {
                            Log.e(FindMeApp.TAG, "ProcessVKFriendsTask", e);
                        }

                        try {
                            user.setIconUrl(userJson.getString("photo_200"));
                        } catch (Exception e) {
                            Log.e(FindMeApp.TAG, "ProcessVKFriendsTask", e);
                        }

                        mUsersBuffer.put(id, user);
                    }
                }

                final Response<UsersModel> usersResponse = FindMeApp.getHTTPManager().checkUsersSync(userModels);

                final UsersModel body = usersResponse.body();
                if (body != null) {
                    for (final Integer userId : body.getUsers()) {
                        final UserMarker user = mUsersBuffer.get(userId);
                        if (user != null) {
                            publishProgress(user);
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(UserMarker... values) {
        if (!isCancelled() && mListener != null) {
            final OnProcessVKFriendsListener listener = mListener.get();
            if (listener != null) {
                listener.onVKFriendLoaded(values[0]);
            }
        }
    }

    @Override
    protected void onCancelled() {
        if (mListener != null) {
            final OnProcessVKFriendsListener listener = mListener.get();
            if (listener != null) {
                listener.onVKFriendsLoadingCanceled();
            }
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mListener != null) {
            final OnProcessVKFriendsListener listener = mListener.get();
            if (listener != null) {
                if (mIsSuccess) {
                    listener.onVKFriendsLoadingCompleted();
                } else {
                    listener.onVKFriendsLoadingFailed();
                }
            }
        }
    }
}
