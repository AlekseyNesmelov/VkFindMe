package com.nesmelov.alexey.vkfindme.tasks;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.ui.markers.UserMarker;

import java.lang.ref.WeakReference;

public class LoadUsersListTask extends AsyncTask<Void, UserMarker, Void> {
    /**
     * User loading listener interface.
     */
    public interface OnLoadUsersListener {
        /**
         * Loading started.
         */
        void onLoadStarted();

        /**
         * User has been loaded.
         *
         * @param user loaded user.
         */
        void onUserLoaded(final UserMarker user);

        /**
         * Loading completed.
         */
        void onLoadCompleted();
    }

    private WeakReference<OnLoadUsersListener> mListener;

    /**
     * Sets task listener.
     *
     * @param listener listener to set.
     */
    public void setListener(final OnLoadUsersListener listener) {
        mListener = new WeakReference<>(listener);
    }

    @Override
    protected void onPreExecute() {
        if (mListener != null) {
            final OnLoadUsersListener listener = mListener.get();
            if (listener != null) {
                listener.onLoadStarted();
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        final UserMarker userMarker = new UserMarker();
        userMarker.setName(FindMeApp.getStorage().getUserName());
        userMarker.setSurname(FindMeApp.getStorage().getUserSurname());
        userMarker.setVkId(FindMeApp.getStorage().getUserVkId());
        userMarker.setIconUrl(FindMeApp.getStorage().getUserIconUrl());

        if (!isCancelled()) {
            publishProgress(userMarker);
        }

        if (!isCancelled()) {
            try (final Cursor usersCursor = FindMeApp.getStorage().getFriends()) {
                if (usersCursor.moveToFirst()) {
                    do {
                        final UserMarker friend = new UserMarker();
                        friend.setVkId(usersCursor.getInt(1));
                        friend.setName(usersCursor.getString(2));
                        friend.setSurname(usersCursor.getString(3));
                        friend.setIconUrl(usersCursor.getString(6));
                        publishProgress(friend);
                    } while (!isCancelled() && usersCursor.moveToNext());
                }
            } catch (Exception e) {
                Log.d(FindMeApp.TAG, "LoadUsersListTask", e);
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(UserMarker... values) {
        if (!isCancelled() && mListener != null) {
            final OnLoadUsersListener listener = mListener.get();
            if (listener != null) {
                listener.onUserLoaded(values[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(final Void users) {
        if (mListener != null) {
            final OnLoadUsersListener listener = mListener.get();
            if (listener != null) {
                listener.onLoadCompleted();
            }
        }
    }
}