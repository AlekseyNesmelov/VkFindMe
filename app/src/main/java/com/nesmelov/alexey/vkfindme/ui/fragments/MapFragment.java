package com.nesmelov.alexey.vkfindme.ui.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.nesmelov.alexey.vkfindme.tasks.GeocoderTask;
import com.nesmelov.alexey.vkfindme.tasks.LoadAlarmsTask;
import com.nesmelov.alexey.vkfindme.tasks.LoadUsersTask;
import com.nesmelov.alexey.vkfindme.tasks.ProcessVKFriendsTask;
import com.nesmelov.alexey.vkfindme.ui.activities.TabHostActivity;
import com.nesmelov.alexey.vkfindme.ui.adapters.AddressListAdapter;
import com.nesmelov.alexey.vkfindme.ui.adapters.AlarmPreviewsAdapter;
import com.nesmelov.alexey.vkfindme.ui.adapters.UserPreviewsAdapter;
import com.nesmelov.alexey.vkfindme.ui.activities.AlarmUsersActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.network.models.UsersModel;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.VKManager;
import com.nesmelov.alexey.vkfindme.services.GpsService;
import com.nesmelov.alexey.vkfindme.services.UpdateFriendsService;
import com.nesmelov.alexey.vkfindme.storage.OnAlarmUpdatedListener;
import com.nesmelov.alexey.vkfindme.storage.OnUserUpdatedListener;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.ui.markers.AlarmMarker;
import com.nesmelov.alexey.vkfindme.ui.markers.UserMarker;
import com.nesmelov.alexey.vkfindme.utils.CircleTransform;
import com.nesmelov.alexey.vkfindme.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        OnAlarmUpdatedListener, OnUserUpdatedListener, UserPreviewsAdapter.OnUserPreviewClickedListener,
        AlarmPreviewsAdapter.OnAlarmPreviewClickedListener, AddressListAdapter.OnAddressClickedListener,
        LoadAlarmsTask.OnLoadAlarmListener, LoadUsersTask.OnLoadUserListener,
        ProcessVKFriendsTask.OnProcessVKFriendsListener, GeocoderTask.GeocoderTaskListener {
    private static final String BUNDLE_ZOOM = "bundle_zoom";
    private static final String BUNDLE_MODE = "bundle_mode";
    private static final int USER_MARKER_SIZE_DP = 50;
    private static final int MODE_USUAL = 0;
    private static final int MODE_SELECT_ALARM_POS = 1;
    private static final int MODE_SELECT_ALARM_RADIUS = 2;
    private static final int GET_ALARM_USERS_REQUEST_CODE = 0;
    private static final int CHANGE_ALARM_USERS_REQUEST_CODE = 1;
    private static final float START_ZOOM = 15f;

    private MapView mMapView;
    private GoogleMap mMap;
    private Circle mAlarmRadius;
    private ImageView mAlarmTarget;
    private SeekBar mRadiusSeekBar;
    private ImageButton mOkBtn;
    private ImageButton mNokBtn;
    private ImageButton mAddFriendsBtn;
    private RecyclerView mAlarmsView;
    private RecyclerView mFriendsPreview;
    private SearchView mSearchView;
    private TextView mMessageView;
    private ProgressBar mAddFriendsProgressBar;
    private TextView mEmptyFriendsMsg;
    private TextView mEmptyAlarmsMsg;

    private VKManager mVKManager;
    private Storage mStorage;

    private int mCurrentMode = MODE_USUAL;

    private UserPreviewsAdapter mUserPreviewsAdapter;
    private AlarmPreviewsAdapter mAlarmPreviewsAdapter;
    private List<AlarmMarker> mAlarmMarkers = new ArrayList<>();
    private List<UserMarker> mUserMarkers = new ArrayList<>();

    private LatLng mStartPos;
    private Float mStartZoom;

    private LoadUsersTask mLoadUsersTask;
    private LoadAlarmsTask mLoadAlarmsTask;
    private GeocoderTask mGeocoderTask;
    private ProcessVKFriendsTask mVKFriendsTask;

    private List<Address> mAddresses = new ArrayList<>();
    private AddressListAdapter mAddressListAdapter;

    private List<Target> mTargets = new CopyOnWriteArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVKManager = FindMeApp.getVKManager();
        mStorage = FindMeApp.getStorage();
        mStorage.setAlarmUpdatedListener(this);
        mStorage.setUserUpdatedListener(this);

        getActivity().startService(new Intent(getActivity(), UpdateFriendsService.class));

        mStartZoom = START_ZOOM;
        if (getActivity() != null) {
            if (savedInstanceState != null) {
                final double lat = savedInstanceState.getDouble(Storage.LAT);
                final double lon = savedInstanceState.getDouble(Storage.LON);
                mStartPos = new LatLng(lat, lon);
                mStartZoom = savedInstanceState.getFloat(BUNDLE_ZOOM);
                mCurrentMode = savedInstanceState.getInt(BUNDLE_MODE);
            } else if (getActivity().getIntent() != null) {
                final String lat = getActivity().getIntent().getStringExtra(Storage.LAT);
                final String lon = getActivity().getIntent().getStringExtra(Storage.LON);
                if (lat != null && lon != null) {
                    mStartPos = new LatLng(
                            Double.parseDouble(lat),
                            Double.parseDouble(lon));
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap !=null) {
            outState.putDouble(Storage.LAT, mMap.getCameraPosition().target.latitude);
            outState.putDouble(Storage.LON, mMap.getCameraPosition().target.longitude);
            outState.putFloat(BUNDLE_ZOOM, mMap.getCameraPosition().zoom);
            outState.putInt(BUNDLE_MODE, mCurrentMode);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.map_page, container, false);
        final RecyclerView addressList = view.findViewById(R.id.search_list);
        final RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        final ImageButton alarmButton = view.findViewById(R.id.alarmBtn);
        mMapView = view.findViewById(R.id.map);
        mFriendsPreview = view.findViewById(R.id.friends_view);
        mAlarmsView = view.findViewById(R.id.alarms_view);
        mSearchView = view.findViewById(R.id.search_view);
        mAddFriendsProgressBar = view.findViewById(R.id.addFriendsProgress);
        mAddFriendsBtn = view.findViewById(R.id.addFriendsBtn);
        mAlarmTarget = view.findViewById(R.id.alarm_target);
        mOkBtn = view.findViewById(R.id.okBtn);
        mNokBtn = view.findViewById(R.id.nokBtn);
        mMessageView = view.findViewById(R.id.messageView);
        mRadiusSeekBar = view.findViewById(R.id.radiusSeekBar);
        mEmptyFriendsMsg = view.findViewById(R.id.empty_friends_message);
        mEmptyAlarmsMsg = view.findViewById(R.id.empty_alarms_message);

        if (mEmptyFriendsMsg != null) {
            mEmptyFriendsMsg.bringToFront();
        }

        if (mEmptyAlarmsMsg != null) {
            mEmptyAlarmsMsg.bringToFront();
        }

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        mAddressListAdapter = new AddressListAdapter(mAddresses, this);
        addressList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        addressList.setAdapter(mAddressListAdapter);

        final int deviceOrientation = this.getResources().getConfiguration().orientation;
        int orientation = LinearLayoutManager.HORIZONTAL;
        if (deviceOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = LinearLayoutManager.VERTICAL;
        }

        mUserPreviewsAdapter = new UserPreviewsAdapter(getActivity(), mUserMarkers, this);
        mFriendsPreview.setLayoutManager(new LinearLayoutManager(getActivity(), orientation, false));
        mFriendsPreview.setAdapter(mUserPreviewsAdapter);

        mAlarmPreviewsAdapter = new AlarmPreviewsAdapter(mAlarmMarkers, this);
        mAlarmsView.setLayoutManager(new LinearLayoutManager(getActivity(), orientation, false));
        mAlarmsView.setAdapter(mAlarmPreviewsAdapter);

        mSearchView.setOnCloseListener(() -> {
            final int itemCount = mAddresses.size();
            mAddresses.clear();
            mAddressListAdapter.notifyItemRangeRemoved(0, itemCount);
            return true;
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                if (mGeocoderTask != null) {
                    mGeocoderTask.cancel(true);
                }
                mGeocoderTask = new GeocoderTask(MapFragment.this.getActivity(), s);
                mGeocoderTask.setListener(MapFragment.this);
                mGeocoderTask.execute();
                return true;
            }
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.showAlarmsBtn:
                    mAlarmsView.setVisibility(View.VISIBLE);
                    if (mEmptyAlarmsMsg != null && mAlarmMarkers.isEmpty()) {
                        mEmptyAlarmsMsg.setVisibility(View.VISIBLE);
                    }
                    mFriendsPreview.setVisibility(View.INVISIBLE);
                    if (mEmptyFriendsMsg != null) {
                        mEmptyFriendsMsg.setVisibility(View.INVISIBLE);
                    }
                    break;
                case R.id.showFriendsBtn:
                    mAlarmsView.setVisibility(View.GONE);
                    if (mEmptyAlarmsMsg != null) {
                        mEmptyAlarmsMsg.setVisibility(View.GONE);
                    }
                    mFriendsPreview.setVisibility(View.VISIBLE);
                    if (mEmptyFriendsMsg != null && mUserMarkers.isEmpty()) {
                        mEmptyFriendsMsg.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        });

        mAddFriendsBtn.setOnClickListener(v -> {
            mAddFriendsBtn.setEnabled(false);
            mAddFriendsProgressBar.setVisibility(View.VISIBLE);
            mVKManager.getFriends(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(final VKResponse response) {
                    if (mVKFriendsTask != null) {
                        mVKFriendsTask.cancel(true);
                    }
                    mVKFriendsTask = new ProcessVKFriendsTask();
                    mVKFriendsTask.setListener(MapFragment.this);
                    mVKFriendsTask.execute(response);
                }

                @Override
                public void onError(final VKError error) {
                    showErrorRefreshFriendsMessage();
                }
            });
        });

        mAlarmTarget.bringToFront();

        alarmButton.setOnClickListener(v -> {
            switch (mCurrentMode) {
                case MODE_USUAL:
                    setMode(MODE_SELECT_ALARM_POS);
                    break;
                case MODE_SELECT_ALARM_POS:
                case MODE_SELECT_ALARM_RADIUS:
                    setMode(MODE_USUAL);
                    break;
            }
        });

        mOkBtn.bringToFront();
        mOkBtn.setOnClickListener(v -> {
            switch (mCurrentMode) {
                case MODE_SELECT_ALARM_POS:
                    setMode(MODE_SELECT_ALARM_RADIUS);
                    break;
                case MODE_SELECT_ALARM_RADIUS:
                    final Intent intent = new Intent(MapFragment.this.getActivity(), AlarmUsersActivity.class);
                    final LatLng latLng = mAlarmRadius.getCenter();
                    intent.putExtra(Storage.LAT, latLng.latitude);
                    intent.putExtra(Storage.LON, latLng.longitude);
                    intent.putExtra(Storage.RADIUS, (float)mAlarmRadius.getRadius());
                    intent.putExtra(Storage.COLOR, Utils.getRandomColor());
                    startActivityForResult(intent, GET_ALARM_USERS_REQUEST_CODE);
                    break;
                default:
                    setMode(MODE_USUAL);
                    break;
            }
        });
        mNokBtn.bringToFront();
        mNokBtn.setOnClickListener(v -> setMode(MODE_USUAL));

        mMessageView.bringToFront();

        mRadiusSeekBar.setProgress(Math.max(0, (int)(mStorage.getAlarmRadius() - Storage.MIN_ALARM_RADIUS)));
        mRadiusSeekBar.bringToFront();
        mRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                final float radius = progress + Storage.MIN_ALARM_RADIUS;
                if (mAlarmRadius != null) {
                    mAlarmRadius.setRadius(radius);
                }
                mStorage.setAlarmRadius(radius);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        mAlarmRadius.getCenter(), getZoomLevel(mAlarmRadius)));
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });

        setMode(mCurrentMode);

        MapsInitializer.initialize(getActivity().getApplicationContext());
        mMapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        setMode(MODE_USUAL);
        switch (requestCode) {
            case GET_ALARM_USERS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    final ArrayList<Integer> users = data.getIntegerArrayListExtra(Storage.USERS);
                    final String names = data.getStringExtra(Storage.NAMES);
                    final double lat = data.getDoubleExtra(Storage.LAT, Storage.BAD_LAT);
                    final double lon = data.getDoubleExtra(Storage.LON, Storage.BAD_LON);
                    final float radius = data.getFloatExtra(Storage.RADIUS, Storage.BAD_RADIUS);
                    final int color = data.getIntExtra(Storage.COLOR, Utils.getRandomColor());
                    final int alarmId = mStorage.addAlarm(lat, lon, radius, color, users);

                    final AlarmMarker alarmMarker = new AlarmMarker(alarmId, lat, lon, radius, color, users, names);
                    addAlarmToInterfaces(alarmMarker);

                    FindMeApp.showToast(getActivity(), getString(R.string.alarm_accepted));
                    getActivity().startService(new Intent(getActivity(), GpsService.class));
                    getActivity().startService(new Intent(getActivity(), UpdateFriendsService.class));
                } else {
                    FindMeApp.showToast(getActivity(), getString(R.string.alarm_canceled));
                }
                break;
            case CHANGE_ALARM_USERS_REQUEST_CODE:
                final int alarmId = data.getIntExtra(Storage.ALARM_ID, Storage.BAD_ID);
                if (resultCode == RESULT_OK) {
                    final ArrayList<Integer> users = data.getIntegerArrayListExtra(Storage.USERS);
                    mStorage.updateAlarm(alarmId, users);
                    AlarmMarker markerToUpdate = null;
                    for (final AlarmMarker marker : mAlarmMarkers) {
                        if (marker.getAlarmId() == alarmId) {
                            markerToUpdate = marker;
                            break;
                        }
                    }
                    if (markerToUpdate != null) {
                        final String names = data.getStringExtra(Storage.NAMES);
                        markerToUpdate.getMarker().setSnippet(names);
                        markerToUpdate.setUsers(users);
                        markerToUpdate.getMarker().hideInfoWindow();
                        FindMeApp.showToast(getActivity(), getString(R.string.alarm_updated));
                    }
                } else if (resultCode == Storage.RESULT_REMOVE) {
                    mStorage.removeAlarm(alarmId);
                    FindMeApp.showToast(getActivity(), getString(R.string.alarm_canceled));
                }
                getActivity().startService(new Intent(getActivity(), GpsService.class));
                getActivity().startService(new Intent(getActivity(), UpdateFriendsService.class));
                break;
        }
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
        mMapView.onDestroy();
        super.onDestroy();
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
        mMap.getUiSettings().setCompassEnabled(false);
        if (ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            final LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
            if (locationManager != null && mStartPos == null) {
                final Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (location != null) {
                    mStorage.setUserLat(location.getLatitude());
                    mStorage.setUserLon(location.getLongitude());
                    final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, START_ZOOM);
                    mMap.moveCamera(cameraUpdate);
                }
            } else {
                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mStartPos, mStartZoom);
                mMap.moveCamera(cameraUpdate);
            }
        }
        mMap.setOnInfoWindowClickListener(marker -> {
            if (marker.getTitle().equals(getString(R.string.alarm))) {
                final Intent intent = new Intent(MapFragment.this.getActivity(), AlarmUsersActivity.class);

                AlarmMarker selectedMarker = null;
                for (final AlarmMarker alarmMarker : mAlarmMarkers) {
                    if (alarmMarker.getMarker().equals(marker)) {
                        selectedMarker = alarmMarker;
                        break;
                    }
                }
                if (selectedMarker != null) {
                    intent.putExtra(Storage.ALARM_ID, selectedMarker.getAlarmId());
                    intent.putExtra(Storage.LAT, selectedMarker.getLat());
                    intent.putExtra(Storage.LON, selectedMarker.getLon());
                    intent.putExtra(Storage.RADIUS, selectedMarker.getRadius());
                    intent.putIntegerArrayListExtra(Storage.USERS, selectedMarker.getUsers());
                    startActivityForResult(intent, CHANGE_ALARM_USERS_REQUEST_CODE);
                }
            }
        });

        addAlarmsFromDataBase();
        addFriendsFromDataBase();
    }

    /**
     * Shows error message.
     */
    private void showErrorRefreshFriendsMessage() {
        FindMeApp.showPopUp(getActivity(), getString(R.string.error_title),
                getString(R.string.refresh_friends_server_error_message));
        mAddFriendsBtn.setEnabled(true);
        mAddFriendsProgressBar.setVisibility(View.GONE);
    }

    /**
     * Sets current mode.
     *
     * @param mode mode to set.
     */
    private synchronized void setMode(final int mode) {
        mCurrentMode = mode;
        switch (mode) {
            case MODE_SELECT_ALARM_POS:
                mRadiusSeekBar.setVisibility(View.INVISIBLE);
                mMessageView.setText(getString(R.string.set_alarm_pos));
                mAlarmTarget.setVisibility(View.VISIBLE);
                mOkBtn.setVisibility(View.VISIBLE);
                mNokBtn.setVisibility(View.VISIBLE);
                break;
            case MODE_SELECT_ALARM_RADIUS:
                mRadiusSeekBar.setVisibility(View.VISIBLE);
                mMessageView.setText(getString(R.string.set_alarm_radius));
                mAlarmTarget.setVisibility(View.GONE);
                mAlarmRadius = mMap.addCircle(new CircleOptions()
                        .center(mMap.getCameraPosition().target)
                        .visible(true)
                        .fillColor(Color.argb(128, 255, 152, 0))
                        .strokeColor(Color.argb(128, 230, 81, 0))
                        .strokeWidth(7)
                        .radius(mStorage.getAlarmRadius())
                );
                final LatLng latLng = mMap.getCameraPosition().target;
                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, getZoomLevel(mAlarmRadius));
                mMap.animateCamera(cameraUpdate);
                break;
            case MODE_USUAL:
            default:
                mRadiusSeekBar.setVisibility(View.INVISIBLE);
                mMessageView.setText("");
                mAlarmTarget.setVisibility(View.GONE);
                mOkBtn.setVisibility(View.GONE);
                mNokBtn.setVisibility(View.GONE);
                if (mAlarmRadius != null) {
                    mAlarmRadius.setVisible(false);
                }
                break;
        }
    }

    /**
     * Gets zoom level for current circle.
     *
     * @param circle circle to calculate zoom.
     * @return zoom level for current circle.
     */
    private float getZoomLevel(final Circle circle) {
        float zoomLevel = 11;
        if (circle != null) {
            double radius = circle.getRadius() + circle.getRadius() / 2;
            double scale = radius / 300;
            zoomLevel = (float) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    @Override
    public void onAlarmRemoved(final int alarmId) {
        AlarmMarker markerToRemove = null;
        int index = 0;
        for (int i = 0; i < mAlarmMarkers.size(); i++) {
            if (mAlarmMarkers.get(i).getAlarmId() == alarmId) {
                markerToRemove = mAlarmMarkers.get(i);
                index = i;
                break;
            }
        }
        if (markerToRemove != null) {
            markerToRemove.getMarker().setVisible(false);
            markerToRemove.getMarker().remove();
            markerToRemove.getCircle().setVisible(false);
            markerToRemove.getCircle().remove();

            mAlarmMarkers.remove(markerToRemove);
            mAlarmPreviewsAdapter.notifyItemRemoved(index);
            if (mEmptyAlarmsMsg != null && mAlarmMarkers.isEmpty() && mAlarmsView.getVisibility() == View.VISIBLE) {
                mEmptyAlarmsMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onAlarmUpdated(final int alarmId) {
        AlarmMarker markerToUpdate = null;
        for (int i = 0; i < mAlarmMarkers.size(); i++) {
            if (mAlarmMarkers.get(i).getAlarmId() == alarmId) {
                markerToUpdate = mAlarmMarkers.get(i);
                break;
            }
        }

        if (markerToUpdate != null) {
            final AlarmMarker marker = mStorage.getAlarmMarker(alarmId);
            if (marker != null) {
                markerToUpdate.getMarker().setSnippet(marker.getNames());
                markerToUpdate.setUsers(marker.getUsers());
                markerToUpdate.getMarker().hideInfoWindow();
            }
        }
    }

    @Override
    public void onDestroyView() {
        mStorage.removeAlarmUpdatedListener();
        mStorage.removeUserUpdatedListener();
        if (mLoadAlarmsTask != null) {
            mLoadAlarmsTask.cancel(true);
            mLoadAlarmsTask = null;
        }
        if (mLoadUsersTask != null) {
            mLoadUsersTask.cancel(true);
            mLoadUsersTask = null;
        }
        if (mVKFriendsTask != null) {
            mVKFriendsTask.cancel(true);
            mVKFriendsTask = null;
        }
        if (mGeocoderTask != null) {
            mGeocoderTask.cancel(true);
            mGeocoderTask = null;
        }
        for (final Target target : mTargets) {
            Picasso.with(getActivity()).cancelRequest(target);
        }
        getActivity().startService(new Intent(getActivity(), UpdateFriendsService.class));
        super.onDestroyView();
    }

    /**
     * Starts loading friends from database.
     */
    private void addFriendsFromDataBase() {
        if (mLoadUsersTask != null) {
            mLoadUsersTask.cancel(true);
        }
        mLoadUsersTask = new LoadUsersTask();
        mLoadUsersTask.setListener(this);
        mLoadUsersTask.execute();
    }

    /**
     * Starts loading alarms from database.
     */
    private void addAlarmsFromDataBase() {
        if (mLoadAlarmsTask != null) {
            mLoadAlarmsTask.cancel(true);
        }
        mLoadAlarmsTask = new LoadAlarmsTask();
        mLoadAlarmsTask.setListener(this);
        mLoadAlarmsTask.execute();
    }

    @Override
    public void onUserUpdated(final int userId, final double lat, final double lon) {
        UserMarker userMarker = null;
        int index = 0;
        for (int i = 0; i < mUserMarkers.size(); i++) {
            if (mUserMarkers.get(i).getVkId() == userId) {
                userMarker = mUserMarkers.get(i);
                index = i;
                break;
            }
        }
        if (userMarker != null && lat != Storage.BAD_LAT && lon != Storage.BAD_LON) {
            if (!userMarker.getVisible()) {
                userMarker.setVisible(true);
                mUserPreviewsAdapter.notifyItemChanged(index);
            }
            userMarker.setLatLon(lat, lon);
        }
    }

    @Override
    public void onUserInvisible(final int userId) {
        UserMarker userMarker = null;
        int index = 0;
        for (int i = 0; i < mUserMarkers.size(); i++) {
            if (mUserMarkers.get(i).getVkId() == userId) {
                userMarker = mUserMarkers.get(i);
                index = i;
                break;
            }
        }
        if (userMarker != null) {
            userMarker.setVisible(false);
            mUserPreviewsAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void onUserPreviewClicked(final UserMarker userMarker) {
        if (userMarker.getVisible() && userMarker.getLat() != Storage.BAD_LAT &&
                userMarker.getLon() != Storage.BAD_LON) {
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(userMarker.getLat(), userMarker.getLon()), mMap.getCameraPosition().zoom);
            mMap.animateCamera(cameraUpdate);
            userMarker.getMarker().showInfoWindow();
        } else {
            FindMeApp.showToast(getActivity(), userMarker.getName()
                    + " " + getString(R.string.is_offline));
        }
    }

    @Override
    public void onAddressClicked(final Address address) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(address.getLatitude(), address.getLongitude()), START_ZOOM));
        final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        }
        mSearchView.setQuery("", false);
        mSearchView.setIconified(true);
        mAddresses.clear();
        mAddressListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAlarmLoaded(final AlarmMarker alarm) {
        addAlarmToInterfaces(alarm);
    }

    @Override
    public void onUserLoaded(final UserMarker user) {
        addUserToInterfaces(user);
    }

    @Override
    public void onVKFriendLoaded(final UserMarker friend) {
        boolean userExists = false;
        for (final UserMarker userMarker : mUserMarkers) {
            if (userMarker.getVkId().equals(friend.getVkId())) {
                userExists = true;
                break;
            }
        }
        if (!userExists) {
            mStorage.addUser(friend);
            addUserToInterfaces(friend);
        }
    }

    @Override
    public void onVKFriendsLoadingCanceled() {
        mAddFriendsBtn.setEnabled(true);
        mAddFriendsProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onVKFriendsLoadingCompleted() {
        FindMeApp.showPopUp(getActivity(), getString(R.string.refresh_friends_title),
                getString(R.string.refresh_friends_message_ok));
        mAddFriendsBtn.setEnabled(true);
        mAddFriendsProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onVKFriendsLoadingFailed() {
        showErrorRefreshFriendsMessage();
    }

    private void addUserToInterfaces(final UserMarker userMarker) {
        if (mEmptyFriendsMsg != null) {
            mEmptyFriendsMsg.setVisibility(View.GONE);
        }
        mUserMarkers.add(userMarker);
        mUserPreviewsAdapter.notifyItemInserted(mUserMarkers.size() - 1);
        userMarker.addToMap(mMap);

        final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                userMarker.setIcon(bitmap);
                mTargets.remove(this);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                mTargets.remove(this);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        mTargets.add(target);
        final int size = Utils.dpToPx(getActivity(), USER_MARKER_SIZE_DP);
        Picasso.with(getActivity())
                .load(userMarker.getIconUrl())
                .transform(new CircleTransform())
                .resize(size, size)
                .into(target);
    }

    private void addAlarmToInterfaces(final AlarmMarker alarm) {
        if (mEmptyAlarmsMsg != null) {
            mEmptyAlarmsMsg.setVisibility(View.GONE);
        }
        mAlarmMarkers.add(alarm);
        alarm.addToMap(getActivity(), mMap);
        mAlarmPreviewsAdapter.notifyItemInserted(mAlarmMarkers.size() - 1);
    }

    @Override
    public void onAlarmPreviewClicked(final AlarmMarker alarm) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(alarm.getLat(), alarm.getLon()), mMap.getCameraPosition().zoom);
        mMap.animateCamera(cameraUpdate);
        alarm.getMarker().showInfoWindow();
    }

    @Override
    public void onGeocodingCompleted(List<Address> addresses) {
        mAddresses.clear();
        mAddresses.addAll(addresses);
        mAddressListAdapter.notifyDataSetChanged();
    }
}
