package com.nesmelov.alexey.vkfindme.ui.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.model.Marker;
import com.nesmelov.alexey.vkfindme.ui.activities.AlarmUsersActivity;
import com.nesmelov.alexey.vkfindme.ui.activities.TabHostActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.network.models.UserModel;
import com.nesmelov.alexey.vkfindme.network.models.UsersModel;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.VKManager;
import com.nesmelov.alexey.vkfindme.services.GpsService;
import com.nesmelov.alexey.vkfindme.services.UpdateFriendsService;
import com.nesmelov.alexey.vkfindme.storage.OnAlarmUpdatedListener;
import com.nesmelov.alexey.vkfindme.storage.OnUserUpdatedListener;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.structures.User;
import com.nesmelov.alexey.vkfindme.ui.AddressListAdapter;
import com.nesmelov.alexey.vkfindme.ui.marker.AlarmMarker;
import com.nesmelov.alexey.vkfindme.ui.marker.UserMarker;
import com.nesmelov.alexey.vkfindme.utils.Utils;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.vk.sdk.VKUIHelper.getApplicationContext;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        OnAlarmUpdatedListener, OnUserUpdatedListener {
    private static final String COLOR_INVISIBLE = "#900000";
    private static final String COLOR_VISIBLE = "#5a924d";

    private static final int ALARM_PREVIEW_SIZE_DP = 42;
    private static final int USER_PREVIEW_SIZE_DP = 65;
    private static final int USER_MARKER_SIZE_DP = 50;

    private static final int MODE_USUAL = 0;
    private static final int MODE_SELECT_ALARM_POS = 1;
    private static final int MODE_SELECT_ALARM_RADIUS = 2;

    private static final int GET_ALARM_USERS_REQUEST_CODE = 0;
    private static final int CHANGE_ALARM_USERS_REQUEST_CODE = 1;

    private static final float START_ZOOM = 15f;

    private MapView mMapView;
    private GoogleMap mMap = null;

    private VKManager mVKManager;
    private HTTPManager mHTTPManager;
    private Storage mStorage;

    private int mCurrentMode = MODE_USUAL;

    private Circle mAlarmRadius;
    private ImageView mAlarmTarget;
    private SeekBar mRadiusSeekBar;
    private ImageButton mOkBtn;
    private ImageButton mNokBtn;

    private ImageButton mAddFriendsBtn;

    private LinearLayout mAlarmPictureLayout;
    private LinearLayout mPictureLayout;
    private TextView mMessageView;
    private ProgressBar mAddFriendsProgressBar;

    private Map<Long, AlarmMarker> mAlarmMarkers = new ConcurrentHashMap<>();
    private Map<Integer, UserMarker> mUserMarkers = new ConcurrentHashMap<>();

    private Map<Integer, User> mUsersBuffer = new ConcurrentHashMap<>();

    private LatLng mStartPos = null;

    private LinearLayout mSearchLayout;
    private LinearLayout mAlarmScrollLayout;

    private GeocoderTask mGeocoderTask = null;

    private List<Address> mAddresses = new CopyOnWriteArrayList<>();
    private AddressListAdapter mAddressListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHTTPManager = FindMeApp.getHTTPManager();
        mVKManager = FindMeApp.getVKManager();
        mStorage = FindMeApp.getStorage();
        mStorage.setAlarmUpdatedListener(this);
        mStorage.setUserUpdatedListener(this);

        final Intent intent = getActivity().getIntent();
        if (intent != null) {
            final String lat = intent.getStringExtra(Storage.LAT);
            final String lon = intent.getStringExtra(Storage.LON);
            if (lat != null && lon != null) {
                mStartPos = new LatLng(
                        Double.parseDouble(lat),
                        Double.parseDouble(lon));
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.map_page, container, false);
        mMapView = view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        final ListView addressList = view.findViewById(R.id.address_list);
        mAddressListAdapter = new AddressListAdapter(getActivity(), mAddresses);
        addressList.setAdapter(mAddressListAdapter);

        addressList.setOnItemClickListener((adapterView, view1, i, l) -> {
            final Address address = mAddresses.get(i);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(address.getLatitude(), address.getLongitude()), START_ZOOM));
            ((TabHostActivity)getActivity()).clickSearchButton();
            final InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
            }
        });

        mSearchLayout = view.findViewById(R.id.search_layout);
        mAlarmScrollLayout = view.findViewById(R.id.verticalScrollLayout);
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                if (mGeocoderTask != null) {
                    mGeocoderTask.cancel(true);
                }
                mGeocoderTask = new GeocoderTask(s);
                mGeocoderTask.execute();

                return true;
            }
        });

        mPictureLayout = view.findViewById(R.id.pictureLinear);
        mAlarmPictureLayout = view.findViewById(R.id.pictureVerticalLinear);
        mAddFriendsProgressBar = view.findViewById(R.id.addFriendsProgress);

        mAddFriendsBtn = view.findViewById(R.id.addFriendsBtn);
        mAddFriendsBtn.setOnClickListener(v -> {
            mAddFriendsBtn.setEnabled(false);
            mAddFriendsProgressBar.setVisibility(View.VISIBLE);
            final VKRequest.VKRequestListener requestListener = new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    try {
                        mUsersBuffer.clear();
                        final JSONObject jsonResponse = response.json.getJSONObject("response");
                        final int count = (int) jsonResponse.getLong("count");

                        final JSONArray usersArray = jsonResponse.getJSONArray("items");
                        final List<Integer> userModels = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            final JSONObject userJson = usersArray.getJSONObject(i);
                            userModels.add(userJson.getInt("id"));
                            final User user = new User();
                            user.setVkId(userJson.getInt("id"));
                            user.setName(userJson.getString("first_name"));
                            user.setSurname(userJson.getString("last_name"));
                            user.setIconUrl(userJson.getString("photo_200"));
                            mUsersBuffer.put(userJson.getInt("id"), user);
                        }

                        mHTTPManager.checkUsers(userModels, new Callback<UsersModel>() {
                            @Override
                            public void onResponse(@NonNull Call<UsersModel> call, @NonNull Response<UsersModel> response) {
                                try {
                                    final UsersModel body = response.body();
                                    if (body != null) {
                                        Log.d("ANESMELOV", body.getUsers().size() + "");
                                        for (final Integer userId : body.getUsers()) {
                                            final User user = mUsersBuffer.get(userId);
                                            if (user != null && !mUserMarkers.containsKey(userId)) {
                                                mHTTPManager.loadImage(user.getIconUrl(), new okhttp3.Callback() {
                                                    @Override
                                                    public void onFailure(okhttp3.Call call, IOException e) {
                                                    }

                                                    @Override
                                                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                                                        if (response.body() != null) {
                                                            final byte[] bytes = response.body().bytes();
                                                            mStorage.addUser(user);
                                                            MapFragment.this.getActivity().runOnUiThread(() -> addUserPreviewIcon(user,
                                                                    BitmapFactory.decodeByteArray(
                                                                            bytes, 0, bytes.length), mMap));
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                    FindMeApp.showPopUp(getActivity(), getString(R.string.refresh_friends_title),
                                            getString(R.string.refresh_friends_message_ok));
                                    mAddFriendsBtn.setEnabled(true);
                                    mAddFriendsProgressBar.setVisibility(View.GONE);

                                } catch (Exception e) {
                                    showErrorRefreshFriendsMessage();
                                }
                            }

                            @Override
                            public void onFailure(Call<UsersModel> call, Throwable t) {
                                showErrorRefreshFriendsMessage();
                            }
                        });
                    } catch (Exception e) {
                        showErrorRefreshFriendsMessage();
                    }
                }

                @Override
                public void onError(VKError error) {
                    showErrorRefreshFriendsMessage();
                }
            };
            mVKManager.getFriends(requestListener);
        });

        mAlarmTarget = view.findViewById(R.id.alarm_target);
        mAlarmTarget.bringToFront();

        final ImageButton alarmButton = view.findViewById(R.id.alarmBtn);
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
        mOkBtn = view.findViewById(R.id.okBtn);
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
        mNokBtn = view.findViewById(R.id.nokBtn);
        mNokBtn.setOnClickListener(v -> setMode(MODE_USUAL));

        mMessageView = view.findViewById(R.id.messageView);
        mMessageView.bringToFront();

        mRadiusSeekBar = view.findViewById(R.id.radiusSeekBar);
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

        setMode(MODE_USUAL);

        MapsInitializer.initialize(getActivity().getApplicationContext());
        mMapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case GET_ALARM_USERS_REQUEST_CODE:
                setMode(MODE_USUAL);
                if (resultCode == RESULT_OK) {
                    final ArrayList<Integer> users = data.getIntegerArrayListExtra(Storage.USERS);
                    final String names = data.getStringExtra(Storage.NAMES);
                    final double lat = data.getDoubleExtra(Storage.LAT, Storage.BAD_LAT);
                    final double lon = data.getDoubleExtra(Storage.LON, Storage.BAD_LON);
                    final float radius = data.getFloatExtra(Storage.RADIUS, Storage.BAD_RADIUS);
                    final int color = data.getIntExtra(Storage.COLOR, Utils.getRandomColor());

                    final long alarmId = mStorage.addAlarm(lat, lon, radius, color, users);
                    final AlarmMarker alarmMarker = new AlarmMarker(alarmId, lat, lon, radius, color, users, names);
                    final Marker mapMarker = alarmMarker.addToMap(getActivity(), mMap);
                    mAlarmMarkers.put(alarmId, alarmMarker);

                    final int size = Utils.dpToPx(getActivity(), ALARM_PREVIEW_SIZE_DP);
                    final Bitmap squareBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable.alarm_miniature), size, size, false);

                    final ImageView imageView = new ImageView(getActivity());
                    final String alarmMarkerId = mapMarker.getId().replace("m", "");
                    imageView.setId(Integer.parseInt(alarmMarkerId));
                    imageView.setPadding(4, 4, 4, 4);
                    imageView.setMinimumHeight(size);
                    imageView.setMinimumWidth(size);
                    imageView.setMaxHeight(size);
                    imageView.setMaxWidth(size);
                    imageView.setImageBitmap(squareBitmap);
                    imageView.setBackgroundColor(color);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setClickable(true);
                    imageView.setOnClickListener(v -> {
                        AlarmMarker alarmMarker1 = null;
                        for (final Long id : mAlarmMarkers.keySet()) {
                            final AlarmMarker marker = mAlarmMarkers.get(id);
                            if (marker.getMarkerId() == v.getId()) {
                                alarmMarker1 = marker;
                                break;
                            }
                        }
                        if (alarmMarker1 != null) {
                            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(alarmMarker1.getLat(), alarmMarker1.getLon()), mMap.getCameraPosition().zoom);
                            mMap.animateCamera(cameraUpdate);
                            alarmMarker1.getMarker().showInfoWindow();
                        }
                    });
                    mAlarmPictureLayout.addView(imageView);

                    FindMeApp.showToast(getActivity(), getString(R.string.alarm_accepted));
                    getActivity().startService(new Intent(getActivity(), GpsService.class));
                } else {
                    FindMeApp.showToast(getActivity(), getString(R.string.alarm_canceled));
                }
                break;
            case CHANGE_ALARM_USERS_REQUEST_CODE:
                setMode(MODE_USUAL);
                final long alarmId = data.getLongExtra(Storage.ALARM_ID, Storage.BAD_ID);
                if (resultCode == RESULT_OK) {
                    final ArrayList<Integer> users = data.getIntegerArrayListExtra(Storage.USERS);
                    mStorage.updateAlarm(alarmId, users);
                    final AlarmMarker markerToUpdate = mAlarmMarkers.get(alarmId);
                    if (markerToUpdate != null) {
                        final String names = data.getStringExtra(Storage.NAMES);
                        markerToUpdate.getMarker().setSnippet(names);
                        markerToUpdate.setUsers(users);
                        markerToUpdate.getMarker().hideInfoWindow();
                        FindMeApp.showToast(getActivity(), getString(R.string.alarm_updated));
                    }
                } else if (resultCode == Storage.RESULT_UPDATE) {
                    final AlarmMarker markerToRemove = mAlarmMarkers.get(alarmId);
                    if (markerToRemove != null) {
                        markerToRemove.getMarker().setVisible(false);
                        markerToRemove.getMarker().remove();
                        markerToRemove.getCircle().setVisible(false);
                        markerToRemove.getCircle().remove();
                        mAlarmMarkers.remove(markerToRemove);
                    }
                    mStorage.removeAlarm(alarmId);

                    FindMeApp.showToast(getActivity(), getString(R.string.alarm_canceled));
                }
                getActivity().startService(new Intent(getActivity(), GpsService.class));
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
                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mStartPos, START_ZOOM);
                mMap.moveCamera(cameraUpdate);
            }
        }
        mMap.setOnInfoWindowClickListener(marker -> {
            if (marker.getTitle().equals(getString(R.string.alarm))) {
                final Intent intent = new Intent(MapFragment.this.getActivity(), AlarmUsersActivity.class);

                AlarmMarker selectedMarker = null;
                for (final Long alarmMarkerId : mAlarmMarkers.keySet()) {
                    final AlarmMarker alarmMarker = mAlarmMarkers.get(alarmMarkerId);
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

        final int size = Utils.dpToPx(getActivity(), ALARM_PREVIEW_SIZE_DP);
        final Bitmap squareBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.alarm_miniature), size, size, false);

        final Map<Long, AlarmMarker> markers = mStorage.getAlarmMarkers();
        for (final Long markerId : markers.keySet()) {
            final AlarmMarker alarmMarker = markers.get(markerId);
            final Marker mapMarker = alarmMarker.addToMap(getActivity(), mMap);

            final ImageView imageView = new ImageView(getActivity());
            final String alarmMarkerId = mapMarker.getId().replace("m", "");
            imageView.setId(Integer.parseInt(alarmMarkerId));
            imageView.setPadding(4, 4, 4, 4);
            imageView.setMinimumHeight(size);
            imageView.setMinimumWidth(size);
            imageView.setMaxHeight(size);
            imageView.setMaxWidth(size);
            imageView.setImageBitmap(squareBitmap);
            imageView.setBackgroundColor(alarmMarker.getColor());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setClickable(true);
            imageView.setOnClickListener(v -> {
                AlarmMarker alarmMarker1 = null;
                for (final Long id : mAlarmMarkers.keySet()) {
                    final AlarmMarker marker = mAlarmMarkers.get(id);
                    if (marker.getMarkerId() == v.getId()) {
                        alarmMarker1 = marker;
                        break;
                    }
                }
                if (alarmMarker1 != null) {
                    final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(alarmMarker1.getLat(), alarmMarker1.getLon()), mMap.getCameraPosition().zoom);
                    mMap.animateCamera(cameraUpdate);
                    alarmMarker1.getMarker().showInfoWindow();
                }
            });
            mAlarmPictureLayout.addView(imageView);
            mAlarmMarkers.put(markerId, alarmMarker);
        }
        addFriendsFromDataBase();

        ((TabHostActivity)getActivity()).hideProgressBar();
    }

    private void showErrorRefreshFriendsMessage() {
        FindMeApp.showPopUp(getActivity(), getString(R.string.error_title),
                getString(R.string.refresh_friends_server_error_message));
        mAddFriendsBtn.setEnabled(true);
        mAddFriendsProgressBar.setVisibility(View.GONE);
    }

    private synchronized void setMode(final int mode) {
        mCurrentMode = mode;
        switch (mode) {
            case MODE_SELECT_ALARM_POS:
                mRadiusSeekBar.setVisibility(View.INVISIBLE);
                mMessageView.setText(getString(R.string.set_alarm_pos));
                mAlarmTarget.animate().alpha(1.0f);
                mOkBtn.animate().alpha(1.0f);
                mNokBtn.animate().alpha(1.0f);
                break;
            case MODE_SELECT_ALARM_RADIUS:
                mRadiusSeekBar.setVisibility(View.VISIBLE);
                mMessageView.setText(getString(R.string.set_alarm_radius));
                mAlarmTarget.animate().alpha(0f);
                mAlarmRadius = mMap.addCircle(new CircleOptions()
                        .center(mMap.getCameraPosition().target)
                        .visible(true)
                        .fillColor(Color.argb(128, 88, 219, 177))
                        .strokeColor(Color.argb(128, 128, 128, 128))
                        .strokeWidth(5)
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
                mAlarmTarget.animate().alpha(0.0f);
                mOkBtn.animate().alpha(0.0f);
                mNokBtn.animate().alpha(0.0f);
                if (mAlarmRadius != null) {
                    mAlarmRadius.setVisible(false);
                }
                break;
        }
    }

    public void showAlarms(final boolean b) {
        if (b) {
            ((TabHostActivity)getActivity()).setCheckedShowSearchBtn(false);
            mSearchLayout.setPivotY(0);
            mSearchLayout.animate().scaleY(0.01f).setDuration(500);
            mSearchLayout.animate().alpha(0).setDuration(500);
        }
        if (b) {
            mAlarmScrollLayout.setPivotY(0);
            mAlarmScrollLayout.animate().scaleY(1f).setDuration(500);
            mAlarmScrollLayout.animate().alpha(1f).setDuration(500);
        } else {
            mAlarmScrollLayout.setPivotY(0);
            mAlarmScrollLayout.animate().scaleY(0.01f).setDuration(500);
            mAlarmScrollLayout.animate().alpha(0).setDuration(500);
        }
    }

    public void showSearchView(final boolean b) {
        if (b) {
            ((TabHostActivity)getActivity()).setCheckedShowDrawerBtn(false);
            mAlarmScrollLayout.setPivotY(0);
            mAlarmScrollLayout.animate().scaleY(0.01f).setDuration(500);
            mAlarmScrollLayout.animate().alpha(0).setDuration(500);
        }
        if (b) {
            mSearchLayout.setPivotY(0);
            mSearchLayout.animate().scaleY(1f).setDuration(500);
            mSearchLayout.animate().alpha(1f).setDuration(500);
        } else {
            mSearchLayout.setPivotY(0);
            mSearchLayout.animate().scaleY(0.01f).setDuration(500);
            mSearchLayout.animate().alpha(0).setDuration(500);
        }
    }

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
    public void onAlarmRemoved(long alarmId) {
        final AlarmMarker markerToRemove = mAlarmMarkers.get(alarmId);
        if (markerToRemove != null) {
            markerToRemove.getMarker().setVisible(false);
            markerToRemove.getMarker().remove();
            markerToRemove.getCircle().setVisible(false);
            markerToRemove.getCircle().remove();
            mAlarmMarkers.remove(alarmId);

            final ImageView previewIcon = mAlarmPictureLayout.findViewById(markerToRemove.getMarkerId());
            if (previewIcon != null) {
                previewIcon.setVisibility(View.GONE);
                mAlarmPictureLayout.removeView(previewIcon);
            }
        }
    }

    @Override
    public void onAlarmUpdated(long alarmId) {
        final AlarmMarker markerToUpdate = mAlarmMarkers.get(alarmId);
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
        getActivity().startService(new Intent(getActivity(), UpdateFriendsService.class));
        super.onDestroyView();
    }

    private void addUserPreviewIcon(final User user, final Bitmap bitmap, final GoogleMap map) {
        final int size = Utils.dpToPx(getActivity(), USER_PREVIEW_SIZE_DP);
        final int circleSize = Utils.dpToPx(getActivity(), USER_MARKER_SIZE_DP);
        final Bitmap squareBitmap;
        final Bitmap circleBitmap;
        if (bitmap == null) {
            squareBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.default_user_icon), size, size, false);
            circleBitmap = Utils.getCroppedBitmap(Bitmap.createScaledBitmap(squareBitmap, circleSize, circleSize, false));
        } else {
            squareBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
            circleBitmap = Utils.getCroppedBitmap(Bitmap.createScaledBitmap(squareBitmap, circleSize, circleSize, false));
        }
        final UserMarker userMarker = new UserMarker(user.getVkId(),user.getName(), user.getSurname(),
                user.getLat(), user.getLon(), user.isVisible(), circleBitmap);

        final ImageView imageView = new ImageView(getActivity());
        final String userId = userMarker.addToMap(map).replace("m", "");

        imageView.setId(Integer.parseInt(userId));
        imageView.setPadding(4, 4, 4, 4);
        imageView.setMinimumHeight(size);
        imageView.setMinimumWidth(size);
        imageView.setMaxHeight(size);
        imageView.setMaxWidth(size);
        imageView.setImageBitmap(squareBitmap);
        if (user.isVisible()) {
            imageView.setBackgroundColor(Color.parseColor(COLOR_VISIBLE));
        } else {
            imageView.setBackgroundColor(Color.parseColor(COLOR_INVISIBLE));
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setClickable(true);
        imageView.setOnClickListener(v -> {
            UserMarker userMarker1 = null;
            for (final Integer id : mUserMarkers.keySet()) {
                final UserMarker marker = mUserMarkers.get(id);
                if (marker.getMarkerId() == v.getId()) {
                    userMarker1 = marker;
                    break;
                }
            }
            if (userMarker1 != null) {
                if (userMarker1.getVisible() && userMarker1.getLat() != Storage.BAD_LAT &&
                        userMarker1.getLon() != Storage.BAD_LON) {
                    final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(userMarker1.getLat(), userMarker1.getLon()), mMap.getCameraPosition().zoom);
                    mMap.animateCamera(cameraUpdate);
                    userMarker1.getMarker().showInfoWindow();
                } else {
                    FindMeApp.showToast(getActivity(), userMarker1.getName() + " " + userMarker1.getSurname()
                            + " " + getString(R.string.is_offline));
                }
            }
        });
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        final int margin = Utils.dpToPx(getActivity(), 2);
        layoutParams.setMargins(margin, margin, margin, margin);

        mPictureLayout.addView(imageView, layoutParams);

        mUserMarkers.put(user.getVkId(), userMarker);
    }

    private void addFriendsFromDataBase() {
        final List<User> friends = mStorage.getFriends();
        for (final User user : friends) {
            mHTTPManager.loadImage(user.getIconUrl(), new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {

                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    final okhttp3.ResponseBody body = response.body();
                    if (body != null) {
                        final byte[] bytes = body.bytes();
                        MapFragment.this.getActivity().runOnUiThread(() -> {
                                    addUserPreviewIcon(user,
                                            BitmapFactory.decodeByteArray(bytes, 0,
                                                    bytes.length), mMap);
                                }
                        );
                    }
                }
            });
        }
    }

    @Override
    public void onUserUpdated(Integer userId, double lat, double lon) {
        final UserMarker userMarker = mUserMarkers.get(userId);
        if (userMarker != null && lat != Storage.BAD_LAT && lon != Storage.BAD_LON) {
            userMarker.setVisible(true);
            userMarker.setLatLon(lat, lon);

            final int markerId = userMarker.getMarkerId();
            final ImageView previewIcon = mPictureLayout.findViewById(markerId);
            if (previewIcon != null) {
                previewIcon.setBackgroundColor(Color.parseColor(COLOR_VISIBLE));
            }
        }
    }

    @Override
    public void onUserInvisible(Integer userId) {
        final UserMarker userMarker = mUserMarkers.get(userId);
        if (userMarker != null) {
            userMarker.setVisible(false);

            final ImageView previewIcon = mPictureLayout.findViewById(userMarker.getMarkerId());
            if (previewIcon != null) {
                previewIcon.setBackgroundColor(Color.parseColor(COLOR_INVISIBLE));
            }
        }
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {
        private static final int MAX_RESULTS = 3;
        private String mPlace;

        public GeocoderTask(final String place) {
            mPlace = place;
        }

        @Override
        protected List<Address> doInBackground(final String... strings) {
            final Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                final List<Address> addresses = geocoder.getFromLocationName(mPlace, MAX_RESULTS);
                if (addresses != null && !addresses.isEmpty()) {
                    return addresses;
                }
            } catch(Exception ignored) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(final List<Address> addresses) {
            super.onPostExecute(addresses);
            if (addresses != null) {
                mAddresses.clear();
                mAddresses.addAll(addresses);
                mAddressListAdapter.notifyDataSetChanged();
            }
        }
    }
}
