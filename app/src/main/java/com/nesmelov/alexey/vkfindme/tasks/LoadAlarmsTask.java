package com.nesmelov.alexey.vkfindme.tasks;

import android.os.AsyncTask;

import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.ui.markers.AlarmMarker;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Alarms loading task.
 */
public class LoadAlarmsTask extends AsyncTask<Void, AlarmMarker, Void> {

    /**
     * Alarms loading listener interface.
     */
    public interface OnLoadAlarmListener {
        /**
         * Method is called when alarm is loaded.
         *
         * @param marker loaded alarm marker.
         */
        void onAlarmLoaded(final AlarmMarker marker);
    }

    private WeakReference<OnLoadAlarmListener>  mListener;

    /**
     * Sets alarm listener.
     *
     * @param listener listener to set.
     */
    public void setListener(final OnLoadAlarmListener listener) {
        mListener = new WeakReference<>(listener);
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!isCancelled()) {
            final List<AlarmMarker> alarmMarkers = FindMeApp.getStorage().getAlarmMarkers();
            for (final AlarmMarker alarmMarker : alarmMarkers) {
                if (!isCancelled()) {
                    publishProgress(alarmMarker);
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(AlarmMarker... values) {
        if (!isCancelled() && mListener != null) {
            final OnLoadAlarmListener listener = mListener.get();
            if (listener != null) {
                listener.onAlarmLoaded(values[0]);
            }
        }
    }
}
