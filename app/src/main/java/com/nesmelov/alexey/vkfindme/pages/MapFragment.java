package com.nesmelov.alexey.vkfindme.pages;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
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
import com.nesmelov.alexey.vkfindme.activities.AlarmUsersActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import com.nesmelov.alexey.vkfindme.network.VKManager;
import com.nesmelov.alexey.vkfindme.services.GpsService;
import com.nesmelov.alexey.vkfindme.storage.Const;
import com.nesmelov.alexey.vkfindme.storage.OnAlarmUpdatedListener;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.structures.User;
import com.nesmelov.alexey.vkfindme.ui.AlarmMarker;
import com.nesmelov.alexey.vkfindme.ui.UserMarker;
import com.nesmelov.alexey.vkfindme.utils.Utils;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;

public class MapFragment extends Fragment implements OnMapReadyCallback, OnUpdateListener, OnAlarmUpdatedListener {
    private static final int USER_PREVIEW_SIZE = 60;
    private static final int USER_MARKER_SIZE = 50;

    private static final int MODE_USUAL = 0;
    private static final int MODE_SELECT_ALARM_POS = 1;
    private static final int MODE_SELECT_ALARM_RADIUS = 2;

    private static final int GET_ALARM_USERS_REQUEST_CODE = 0;
    private static final int CHANGE_ALARM_USERS_REQUEST_CODE = 1;

    private static final float START_ZOOM = 15f;

    private MapView mMapView;
    private GoogleMap mMap = null;
    private LocationManager mLocationManager;

    private VKManager mVKManager;
    private HTTPManager mHTTPManager;
    private Storage mStorage;
    private int mCurrentMode = MODE_USUAL;

    private ToggleButton mVisibilityBtn;
    private CompoundButton.OnCheckedChangeListener mVisibilityBtnListener;

    private ImageView mAlarmTarget;
    private SeekBar mRadiusSeekBar;
    private ImageButton mRefreshFriendsBtn;
    private ImageButton mAlarmButton;
    private ImageButton mOkBtn;
    private ImageButton mNokBtn;

    private LinearLayout mPictureLayout;

    private TextView mMessageView;

    private Circle mAlarmRadius;

    private List<AlarmMarker> mAlarmMarkers = new CopyOnWriteArrayList<>();
    private Map<Long, UserMarker> mUserMarkers = new ConcurrentHashMap<>();

    private Map<Long, User> mUsersBuffer = new ConcurrentHashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHTTPManager = FindMeApp.getHTTPManager();
        mVKManager = FindMeApp.getVKManager();
        mStorage = FindMeApp.getStorage();
        mStorage.addAlarmRemovedListener(this);

        getActivity().startService(new Intent(getContext(), GpsService.class));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.map_page, container, false);
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        mPictureLayout = (LinearLayout) view.findViewById(R.id.pictureLinear);

        mRefreshFriendsBtn = (ImageButton) view.findViewById(R.id.refreshBtn);
        mRefreshFriendsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final VKRequest.VKRequestListener requestListener = new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        try {
                            mUsersBuffer.clear();
                            final JSONObject jsonResponse = response.json.getJSONObject("response");
                            final int count = (int) jsonResponse.getLong("count");

                            final JSONArray usersArray = jsonResponse.getJSONArray("items");
                            final StringBuilder idToCheck = new StringBuilder();
                            for (int i = 0; i < count; i++) {
                                final JSONObject userJson = usersArray.getJSONObject(i);
                                idToCheck.append(userJson.getLong("id")).append(";");
                                final User user = new User();
                                user.setVkId(userJson.getInt("id"));
                                user.setName(userJson.getString("first_name"));
                                user.setSurname(userJson.getString("last_name"));
                                user.setIconUrl(userJson.getString("photo_200"));
                                mUsersBuffer.put(userJson.getLong("id"), user);
                            }
                            mHTTPManager.executeRequest(HTTPManager.REQUEST_CHECK_USERS, HTTPManager.REQUEST_CHECK_USERS,
                                    MapFragment.this, idToCheck.toString());
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onError(VKError error) {
                        MapFragment.this.onError(HTTPManager.REQUEST_CHECK_USERS, HTTPManager.SERVER_ERROR_CODE);
                    }
                };
                mVKManager.executeRequest(VKManager.REQUEST_GET_FRIENDS, requestListener);
            }
        });

        mVisibilityBtn = (ToggleButton) view.findViewById(R.id.visibleBtn);
        mVisibilityBtnListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Integer user = mStorage.getUserVkId();
                if (isChecked) {
                    mHTTPManager.executeRequest(HTTPManager.REQUEST_SET_VISIBILITY_TRUE,
                            HTTPManager.REQUEST_SET_VISIBILITY_FALSE,
                            MapFragment.this, user.toString(),
                            String.valueOf(mStorage.getUserLat()),
                            String.valueOf(mStorage.getUserLon()));
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
        mAlarmTarget.bringToFront();

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
        mOkBtn = (ImageButton) view.findViewById(R.id.okBtn);
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case MODE_SELECT_ALARM_POS:
                        setMode(MODE_SELECT_ALARM_RADIUS);
                        break;
                    case MODE_SELECT_ALARM_RADIUS:
                        final Intent intent = new Intent(MapFragment.this.getContext(), AlarmUsersActivity.class);
                        final LatLng latLng = mAlarmRadius.getCenter();
                        intent.putExtra(Const.LAT,latLng.latitude);
                        intent.putExtra(Const.LON,latLng.longitude);
                        intent.putExtra(Const.RADIUS, (float)mAlarmRadius.getRadius());
                        startActivityForResult(intent, GET_ALARM_USERS_REQUEST_CODE);
                        break;
                    default:
                        setMode(MODE_USUAL);
                        break;
                }
            }
        });
        mNokBtn = (ImageButton) view.findViewById(R.id.nokBtn);
        mNokBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(MODE_USUAL);
            }
        });

        mMessageView = (TextView) view.findViewById(R.id.messageView);
        mMessageView.bringToFront();

        mRadiusSeekBar = (SeekBar) view.findViewById(R.id.radiusSeekBar);
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
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GET_ALARM_USERS_REQUEST_CODE:
                setMode(MODE_USUAL);
                if (resultCode == RESULT_OK) {
                    final ArrayList<Integer> users = data.getIntegerArrayListExtra(Const.USERS);
                    final String names = data.getStringExtra(Const.NAMES);
                    final double lat = data.getDoubleExtra(Const.LAT, Const.BAD_LAT);
                    final double lon = data.getDoubleExtra(Const.LON, Const.BAD_LON);
                    final float radius = data.getFloatExtra(Const.RADIUS, Const.BAD_RADIUS);

                    final long alarmId = mStorage.addAlarm(lat, lon, radius, users);
                    mAlarmMarkers.add(new AlarmMarker(getContext(), alarmId, lat, lon, radius, users, names, mMap));

                    FindMeApp.showToast(getContext(), getString(R.string.alarm_accepted));
                    getActivity().startService(new Intent(getContext(), GpsService.class));
                } else {
                    FindMeApp.showToast(getContext(), getString(R.string.alarm_canceled));
                }
                break;
            case CHANGE_ALARM_USERS_REQUEST_CODE:
                setMode(MODE_USUAL);
                final long alarmId = data.getLongExtra(Const.ALARM_ID, Const.BAD_ID);
                if (resultCode == RESULT_OK) {
                    final ArrayList<Integer> users = data.getIntegerArrayListExtra(Const.USERS);
                    mStorage.updateAlarm(alarmId, users);
                    AlarmMarker markerToUpdate = null;
                    for (final AlarmMarker alarmMarker : mAlarmMarkers) {
                        if (alarmMarker.getAlarmId() == alarmId) {
                            markerToUpdate = alarmMarker;
                            break;
                        }
                    }
                    if (markerToUpdate != null) {
                        final String names = data.getStringExtra(Const.NAMES);
                        markerToUpdate.getMarker().setSnippet(names);
                        markerToUpdate.setUsers(users);
                        markerToUpdate.getMarker().hideInfoWindow();
                        FindMeApp.showToast(getContext(), getString(R.string.alarm_updated));
                    }
                } else {
                    AlarmMarker markerToRemove = null;
                    for (final AlarmMarker alarmMarker : mAlarmMarkers) {
                        if (alarmMarker.getAlarmId() == alarmId) {
                            markerToRemove = alarmMarker;
                            break;
                        }
                    }
                    if (markerToRemove != null) {
                        markerToRemove.getMarker().setVisible(false);
                        markerToRemove.getMarker().remove();
                        mAlarmMarkers.remove(markerToRemove);
                    }
                    mStorage.removeAlarm(alarmId);

                    FindMeApp.showToast(getContext(), getString(R.string.alarm_canceled));
                }
                getActivity().startService(new Intent(getContext(), GpsService.class));
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
        if (ActivityCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
            final Location location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (location != null) {
                final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, START_ZOOM);
                mMap.moveCamera(cameraUpdate);
            }
        }
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                if (marker.getTitle().equals(getString(R.string.alarm))) {
                    final Intent intent = new Intent(MapFragment.this.getContext(), AlarmUsersActivity.class);

                    AlarmMarker selectedMarker = null;
                    for (final AlarmMarker alarmMarker : mAlarmMarkers) {
                        if (alarmMarker.getMarker().equals(marker)) {
                            selectedMarker = alarmMarker;
                            break;
                        }
                    }
                    if (selectedMarker != null) {
                        intent.putExtra(Const.ALARM_ID, selectedMarker.getAlarmId());
                        intent.putExtra(Const.LAT, selectedMarker.getLat());
                        intent.putExtra(Const.LON, selectedMarker.getLon());
                        intent.putExtra(Const.RADIUS, selectedMarker.getRadius());
                        intent.putIntegerArrayListExtra(Const.USERS, selectedMarker.getUsers());
                        startActivityForResult(intent, CHANGE_ALARM_USERS_REQUEST_CODE);
                    }
                }
            }
        });
        mAlarmMarkers.addAll(mStorage.getAlarmMarkers(getContext(), mMap));
        addFriendsFromDataBase();
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
                getActivity().startService(new Intent(getContext(), GpsService.class));
                break;
            case HTTPManager.REQUEST_CHECK_USERS:
                try {
                    final JSONArray users = update.getJSONArray("users");
                    for (int i = 0; i < users.length(); i++) {
                        final Long userId = users.getLong(i);
                        final User user = mUsersBuffer.get(userId);
                        if(user != null && !mUserMarkers.containsKey(userId)) {
                            final ImageLoader.ImageListener listener = new ImageLoader.ImageListener() {
                                @Override
                                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                    if (response.getBitmap() != null) {
                                        mStorage.addUser(user);
                                        addUserPreviewIcon(user, response.getBitmap(), mMap);
                                    }
                                }

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            };
                            mHTTPManager.asyncLoadBitmap(user.getIconUrl(), listener);
                        }
                    }
                    FindMeApp.showPopUp(getContext(), getString(R.string.refresh_friends_title),
                            getString(R.string.refresh_friends_message_ok));
                } catch (JSONException e) {
                    onError(HTTPManager.REQUEST_CHECK_USERS, HTTPManager.SERVER_ERROR_CODE);
                }
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
                mStorage.setVisibility(false);
                getActivity().startService(new Intent(getContext(), GpsService.class));
                break;
            case HTTPManager.REQUEST_CHECK_USERS:
                FindMeApp.showPopUp(getContext(), getString(R.string.error_title),
                        getString(R.string.refresh_friends_server_error_message));
                break;
            default:
                break;
        }
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
        AlarmMarker markerToRemove = null;
        for (final AlarmMarker alarmMarker : mAlarmMarkers) {
            if (alarmMarker.getAlarmId() == alarmId) {
                markerToRemove = alarmMarker;
                break;
            }
        }
        if (markerToRemove != null) {
            markerToRemove.getMarker().setVisible(false);
            markerToRemove.getMarker().remove();
            mAlarmMarkers.remove(markerToRemove);
        }
    }

    @Override
    public void onAlarmUpdated(long alarmId) {
        AlarmMarker markerToUpdate = null;
        for (final AlarmMarker alarmMarker : mAlarmMarkers) {
            if (alarmMarker.getAlarmId() == alarmId) {
                markerToUpdate = alarmMarker;
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
        super.onDestroyView();
        mStorage.removeAlarmRemovedListener(this);
    }

    private void addUserPreviewIcon(final User user, final Bitmap bitmap, final GoogleMap map) {
        final int size = Utils.dpToPx(getContext(), USER_PREVIEW_SIZE);
        final int circleSize = Utils.dpToPx(getContext(), USER_MARKER_SIZE);
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
        final UserMarker userMarker = new UserMarker(user.getVkId(),
                Const.BAD_LAT, Const.BAD_LON, user.getName(), user.getSurname(), circleBitmap);

        final ImageView imageView = new ImageView(getContext());
        final String userId = userMarker.addOnMap(map).replace("m", "");

        imageView.setId(Integer.parseInt(userId));
        imageView.setPadding(4, 4, 4, 4);
        imageView.setMinimumHeight(size);
        imageView.setMinimumWidth(size);
        imageView.setMaxHeight(size);
        imageView.setMaxWidth(size);
        imageView.setImageBitmap(squareBitmap);

        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserMarker userMarker = null;
                for (final Long id : mUserMarkers.keySet()) {
                    final UserMarker marker = mUserMarkers.get(id);
                    if (marker.getMarkerId() == v.getId()) {
                        userMarker = marker;
                        break;
                    }
                }
                mUserMarkers.get((long)v.getId());
                if (userMarker != null) {
                    final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(userMarker.getLat(), userMarker.getLon()), getZoomLevel(mAlarmRadius));
                    mMap.animateCamera(cameraUpdate);
                }
            }
        });
        mPictureLayout.addView(imageView);

        mUserMarkers.put((long)user.getVkId(), userMarker);
    }

    private void addFriendsFromDataBase() {
        final List<User> friend = mStorage.getFriends();
        for (final User user : friend) {
            final ImageLoader.ImageListener listener = new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null) {
                        addUserPreviewIcon(user, response.getBitmap(), mMap);
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                }
            };
            mHTTPManager.asyncLoadBitmap(user.getIconUrl(), listener);
        }
    }
}
