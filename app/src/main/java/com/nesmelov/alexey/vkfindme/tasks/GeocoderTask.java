package com.nesmelov.alexey.vkfindme.tasks;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.nesmelov.alexey.vkfindme.application.FindMeApp;

import java.lang.ref.WeakReference;
import java.util.List;

public class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

    /**
     * Geocoder task listener.
     */
    public interface GeocoderTaskListener {
        /**
         * Geocoding completed.
         *
         * @param addresses found addresses.
         */
        void onGeocodingCompleted(final List<Address> addresses);
    }

    private static final int MAX_RESULTS = 3;
    private String mPlace;
    private String mCountry;
    private WeakReference<Context> mContext;
    private WeakReference<GeocoderTaskListener> mListener;

    /**
     * Constructs geocoder task.
     *
     * @param context context to use.
     * @param place place phrase to use for searching.
     */
    public GeocoderTask(final Context context, final String place) {
        mPlace = place;
        mContext = new WeakReference<>(context);
    }

    /**
     * Sets geocoder listener.
     *
     * @param listener listener to set.
     */
    public void setListener(final GeocoderTaskListener listener) {
        mListener = new WeakReference<>(listener);
    }

    @Override
    protected List<Address> doInBackground(final String... strings) {
        try {
            final Geocoder geocoder = new Geocoder(mContext.get());
            final List<Address> addressesForCity = geocoder.getFromLocation(FindMeApp.getStorage().getUserLat(),
                    FindMeApp.getStorage().getUserLon(), 1);
            if (addressesForCity != null && !addressesForCity.isEmpty()) {
                mCountry = addressesForCity.get(0).getCountryName();
            }
            if (mCountry != null && !mPlace.contains(mCountry) && !mPlace.isEmpty()) {
                mPlace = mCountry + ", " + mPlace;
            }
            final List<Address> addresses = geocoder.getFromLocationName(mPlace, MAX_RESULTS);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses;
            }
        } catch(Exception e) {
            Log.e(FindMeApp.TAG, "GeocoderTask", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(final List<Address> addresses) {
        if (mListener != null) {
            final GeocoderTaskListener listener = mListener.get();
            if (listener != null && addresses != null) {
                listener.onGeocodingCompleted(addresses);
            }
        }
    }
}