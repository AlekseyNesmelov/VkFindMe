package com.nesmelov.alexey.vkfindme.tasks;

import android.os.AsyncTask;
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
            try {
                final JSONObject jsonResponse = response.json.getJSONObject("response");
                final int count = (int) jsonResponse.getLong("count");

                final JSONArray usersArray = jsonResponse.getJSONArray("items");
                final List<Integer> userModels = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    final UserMarker user = new UserMarker();
                    final JSONObject userJson = usersArray.getJSONObject(i);
                    final int id = userJson.getInt("id");
                    userModels.add(id);
                    user.setVkId(id);
                    user.setName(userJson.getString("first_name"));
                    user.setSurname(userJson.getString("last_name"));
                    user.setIconUrl(userJson.getString("photo_200"));
                    mUsersBuffer.put(id, user);
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
            } catch (Exception e) {
                mIsSuccess = false;
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
