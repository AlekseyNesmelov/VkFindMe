package com.nesmelov.alexey.vkfindme.tasks;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.ui.markers.UserMarker;

import java.lang.ref.WeakReference;

/**
 * Users loading task.
 */
public class LoadUsersTask extends AsyncTask<Void, UserMarker, Void> {

    /**
     * User loded listener interface.
     */
    public interface OnLoadUserListener {
        /**
         * This method is called when an user is loaded.
         *
         * @param user loaded user.
         */
        void onUserLoaded(final UserMarker user);
    }

    private WeakReference<OnLoadUserListener> mListener;

    /**
     * Sets user loaded listener.
     *
     * @param listener listener to set.
     */
    public void setListener(final OnLoadUserListener listener) {
        mListener = new WeakReference<>(listener);
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!isCancelled()) {
            try (final Cursor usersCursor = FindMeApp.getStorage().getFriends()) {
                if (usersCursor.moveToFirst()) {
                    do {
                        final UserMarker friend = new UserMarker(
                                usersCursor.getInt(1),
                                usersCursor.getString(2),
                                usersCursor.getString(3),
                                usersCursor.getDouble(4),
                                usersCursor.getDouble(5)
                        );
                        friend.setIconUrl(usersCursor.getString(6));
                        friend.setVisible(usersCursor.getInt(7) == 1);
                        publishProgress(friend);
                    } while (!isCancelled() && usersCursor.moveToNext());
                }
            } catch (Exception e) {
                Log.e(FindMeApp.TAG, "LoadUsersListTask", e);
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(UserMarker... values) {
        if (!isCancelled() && mListener != null) {
            final OnLoadUserListener listener = mListener.get();
            if (listener != null) {
                listener.onUserLoaded(values[0]);
            }
        }
    }
}