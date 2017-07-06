package com.nesmelov.alexey.vkfindme.pages;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import com.nesmelov.alexey.vkfindme.services.GpsService;
import com.nesmelov.alexey.vkfindme.storage.Storage;

import org.json.JSONObject;

import static android.content.Context.LOCATION_SERVICE;

public class MapFragment extends Fragment implements OnMapReadyCallback, OnUpdateListener {
    private static final int MODE_USUAL = 0;
    private static final int MODE_SELECT_ALARM_POS = 1;
    private static final int MODE_SELECT_ALARM_RADIUS = 2;

    private static final int START_ZOOM = 15;

    private MapView mMapView;
    private GoogleMap mMap = null;
    private LocationManager mLocationManager;

    private HTTPManager mHTTPManager;
    private Storage mStorage;
    private int mCurrentMode = MODE_USUAL;

    private ToggleButton mVisibilityBtn;
    private CompoundButton.OnCheckedChangeListener mVisibilityBtnListener;

    private ImageView mAlarmTarget;

    private ImageButton mAlarmButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHTTPManager = FindMeApp.getHTTPManager();
        mStorage = FindMeApp.getStorage();


        mHTTPManager.executeRequest(HTTPManager.REQUEST_ADD_USER,
                HTTPManager.REQUEST_IDLE,
                MapFragment.this, "123");

        if (mStorage.getVisibility()) {
            getActivity().startService(new Intent(getContext(), GpsService.class));
        } else {
            getActivity().stopService(new Intent(getContext(), GpsService.class));
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.map_page, container, false);
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        mVisibilityBtn = (ToggleButton) view.findViewById(R.id.visibleBtn);
        mVisibilityBtnListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Integer user = mStorage.getUser();
                if (isChecked) {
                    mHTTPManager.executeRequest(HTTPManager.REQUEST_SET_VISIBILITY_TRUE,
                            HTTPManager.REQUEST_SET_VISIBILITY_FALSE,
                            MapFragment.this, user.toString(), "0", "0");
                } else {
                    mHTTPManager.executeRequest(HTTPManager.REQUEST_SET_VISIBILITY_FALSE,
                            HTTPManager.REQUEST_SET_VISIBILITY_TRUE,
                            MapFragment.this, user.toString());
                }
            }
        };
        mVisibilityBtn.setChecked(mStorage.getVisibility());
        mVisibilityBtn.setOnCheckedChangeListener(mVisibilityBtnListener);

        mAlarmTarget = (ImageView) view.findViewById(R.id.alarm_target);
        setMode(MODE_USUAL);

        mAlarmButton = (ImageButton) view.findViewById(R.id.alarmBtn);
        mAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case MODE_USUAL:
                        setMode(MODE_SELECT_ALARM_POS);
                        break;
                    case MODE_SELECT_ALARM_POS:
                    case MODE_SELECT_ALARM_RADIUS:
                        setMode(MODE_USUAL);
                        break;
                }
            }
        });

        MapsInitializer.initialize(getActivity().getApplicationContext());
        mMapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
            final Location location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (location != null) {
                final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, START_ZOOM);
                mMap.moveCamera(cameraUpdate);

                /*final Circle circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(location.getLatitude(), location.getLongitude()))
                        .radius(1000)
                        .strokeWidth(10)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.argb(128, 255, 0, 0))
                        .clickable(true));*/
            }
        }
    }

    @Override
    public void onUpdate(final int request, final JSONObject update) {
        switch (request) {
            case HTTPManager.REQUEST_ADD_USER:
                break;
            case HTTPManager.REQUEST_SET_VISIBILITY_TRUE:
                FindMeApp.showToast(getContext(), getString(R.string.visibility_true_message));
                mStorage.setVisibility(true);
                getActivity().startService(new Intent(getContext(), GpsService.class));
                break;
            case HTTPManager.REQUEST_SET_VISIBILITY_FALSE:
                FindMeApp.showToast(getContext(), getString(R.string.visibility_false_message));
                mStorage.setVisibility(false);
                getActivity().stopService(new Intent(getContext(), GpsService.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onError(final int request, final int errorCode) {
        switch (request) {
            case HTTPManager.REQUEST_ADD_USER:
                break;
            case HTTPManager.REQUEST_SET_VISIBILITY_TRUE:
                FindMeApp.showPopUp(getContext(), getString(R.string.error_title),
                        getString(R.string.on_visibility_server_error_message));
                mVisibilityBtn.setOnCheckedChangeListener(null);
                mVisibilityBtn.setChecked(mStorage.getVisibility());
                mVisibilityBtn.setOnCheckedChangeListener(mVisibilityBtnListener);
                break;
            case HTTPManager.REQUEST_SET_VISIBILITY_FALSE:
                FindMeApp.showPopUp(getContext(), getString(R.string.error_title),
                        getString(R.string.off_visibility_server_error_message));
                getActivity().stopService(new Intent(getContext(), GpsService.class));
                mStorage.setVisibility(false);
                break;
            default:
                break;
        }
    }

    private void setMode(final int mode) {
        mCurrentMode = mode;
        switch (mode) {
            case MODE_SELECT_ALARM_POS:
                mAlarmTarget.animate().alpha(1.0f);
                break;
            case MODE_SELECT_ALARM_RADIUS:
                break;
            case MODE_USUAL:
            default:
                mAlarmTarget.animate().alpha(0.0f);
                break;
        }
    }
}
