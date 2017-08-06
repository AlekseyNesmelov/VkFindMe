package com.nesmelov.alexey.vkfindme.pages;

import android.Manifest;
import android.app.Fragment;
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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
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
import com.nesmelov.alexey.vkfindme.activities.TabHostActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import com.nesmelov.alexey.vkfindme.network.VKManager;
import com.nesmelov.alexey.vkfindme.services.GpsService;
import com.nesmelov.alexey.vkfindme.services.UpdateFriendsService;
import com.nesmelov.alexey.vkfindme.storage.Const;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.vk.sdk.VKUIHelper.getApplicationContext;

public class MapFragment extends Fragment implements OnMapReadyCallback, OnUpdateListener,
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
    private LocationManager mLocationManager;

    private VKManager mVKManager;
    private HTTPManager mHTTPManager;
    private Storage mStorage;

    private int mCurrentMode = MODE_USUAL;

    private Circle mAlarmRadius;
    private ImageView mAlarmTarget;
    private SeekBar mRadiusSeekBar;
    private ImageButton mAlarmButton;
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
    private SearchView mSearchView;

    private GeocoderTask mGeocoderTask = null;

    private ListView mAddressList;
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
            final String lat = intent.getStringExtra(Const.LAT);
            final String lon = intent.getStringExtra(Const.LON);
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
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        mAddressList = (ListView) view.findViewById(R.id.address_list);
        mAddressListAdapter = new AddressListAdapter(getActivity(), mAddresses);
        mAddressList.setAdapter(mAddressListAdapter);

        mAddressList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Address address = mAddresses.get(i);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(address.getLatitude(), address.getLongitude()), START_ZOOM));
            }
        });

        mSearchLayout = (LinearLayout) view.findViewById(R.id.search_layout);
        mAlarmScrollLayout = (LinearLayout) view.findViewById(R.id.verticalScrollLayout);
        mSearchView = (SearchView) view.findViewById(R.id.search_view);
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
                mGeocoderTask = new GeocoderTask(s);
                mGeocoderTask.execute();

                return true;
            }
        });

        mPictureLayout = (LinearLayout) view.findViewById(R.id.pictureLinear);
        mAlarmPictureLayout = (LinearLayout) view.findViewById(R.id.pictureVerticalLinear);
        mAddFriendsProgressBar = (ProgressBar) view.findViewById(R.id.addFriendsProgress);

        mAddFriendsBtn = (ImageButton) view.findViewById(R.id.addFriendsBtn);
        mAddFriendsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mAddFriendsBtn.setEnabled(false);
                mAddFriendsProgressBar.setVisibility(View.VISIBLE);
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
                                mUsersBuffer.put(userJson.getInt("id"), user);
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
                        final Intent intent = new Intent(MapFragment.this.getActivity(), AlarmUsersActivity.class);
                        final LatLng latLng = mAlarmRadius.getCenter();
                        intent.putExtra(Const.LAT,latLng.latitude);
                        intent.putExtra(Const.LON,latLng.longitude);
                        intent.putExtra(Const.RADIUS, (float)mAlarmRadius.getRadius());
                        intent.putExtra(Const.COLOR, Utils.getRandomColor());
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
                    final int color = data.getIntExtra(Const.COLOR, Utils.getRandomColor());

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
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlarmMarker alarmMarker = null;
                            for (final Long id : mAlarmMarkers.keySet()) {
                                final AlarmMarker marker = mAlarmMarkers.get(id);
                                if (marker.getMarkerId() == v.getId()) {
                                    alarmMarker = marker;
                                    break;
                                }
                            }
                            if (alarmMarker != null) {
                                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(alarmMarker.getLat(), alarmMarker.getLon()), mMap.getCameraPosition().zoom);
                                mMap.animateCamera(cameraUpdate);
                                alarmMarker.getMarker().showInfoWindow();
                            }
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
                final long alarmId = data.getLongExtra(Const.ALARM_ID, Const.BAD_ID);
                if (resultCode == RESULT_OK) {
                    final ArrayList<Integer> users = data.getIntegerArrayListExtra(Const.USERS);
                    mStorage.updateAlarm(alarmId, users);
                    final AlarmMarker markerToUpdate = mAlarmMarkers.get(alarmId);
                    if (markerToUpdate != null) {
                        final String names = data.getStringExtra(Const.NAMES);
                        markerToUpdate.getMarker().setSnippet(names);
                        markerToUpdate.setUsers(users);
                        markerToUpdate.getMarker().hideInfoWindow();
                        FindMeApp.showToast(getActivity(), getString(R.string.alarm_updated));
                    }
                } else if (resultCode == Const.RESULT_UPDATE) {
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
            mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
            if (mStartPos == null) {
                final Location location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
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
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
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

        final int size = Utils.dpToPx(getActivity(), ALARM_PREVIEW_SIZE_DP);
        final Bitmap squareBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.alarm_miniature), size, size, false);

        final Map<Long, AlarmMarker> markers = mStorage.getAlarmMarkers(getActivity(), mMap);
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
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlarmMarker alarmMarker = null;
                    for (final Long id : mAlarmMarkers.keySet()) {
                        final AlarmMarker marker = mAlarmMarkers.get(id);
                        if (marker.getMarkerId() == v.getId()) {
                            alarmMarker = marker;
                            break;
                        }
                    }
                    if (alarmMarker != null) {
                        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                new LatLng(alarmMarker.getLat(), alarmMarker.getLon()), mMap.getCameraPosition().zoom);
                        mMap.animateCamera(cameraUpdate);
                        alarmMarker.getMarker().showInfoWindow();
                    }
                }
            });
            mAlarmPictureLayout.addView(imageView);
            mAlarmMarkers.put(markerId, alarmMarker);
        }
        addFriendsFromDataBase();

        ((TabHostActivity)getActivity()).hideProgressBar();
    }

    @Override
    public void onUpdate(final int request, final JSONObject update) {
        switch (request) {
            case HTTPManager.REQUEST_ADD_USER:
                break;
            case HTTPManager.REQUEST_CHECK_USERS:
                try {
                    final JSONArray users = update.getJSONArray("users");
                    for (int i = 0; i < users.length(); i++) {
                        final Integer userId = users.getInt(i);
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
                    FindMeApp.showPopUp(getActivity(), getString(R.string.refresh_friends_title),
                            getString(R.string.refresh_friends_message_ok));
                    mAddFriendsBtn.setEnabled(true);
                    mAddFriendsProgressBar.setVisibility(View.GONE);
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
            case HTTPManager.REQUEST_CHECK_USERS:
                FindMeApp.showPopUp(getActivity(), getString(R.string.error_title),
                        getString(R.string.refresh_friends_server_error_message));
                mAddFriendsBtn.setEnabled(true);
                mAddFriendsProgressBar.setVisibility(View.GONE);
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

            final ImageView previewIcon = (ImageView) mAlarmPictureLayout.findViewById(markerToRemove.getMarkerId());
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
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserMarker userMarker = null;
                for (final Integer id : mUserMarkers.keySet()) {
                    final UserMarker marker = mUserMarkers.get(id);
                    if (marker.getMarkerId() == v.getId()) {
                        userMarker = marker;
                        break;
                    }
                }
                if (userMarker != null) {
                    if (userMarker.getVisible() && userMarker.getLat() != Const.BAD_LAT &&
                            userMarker.getLon() != Const.BAD_LON) {
                        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                new LatLng(userMarker.getLat(), userMarker.getLon()), mMap.getCameraPosition().zoom);
                        mMap.animateCamera(cameraUpdate);
                        userMarker.getMarker().showInfoWindow();
                    } else {
                        FindMeApp.showToast(getActivity(), userMarker.getName() + " " + userMarker.getSurname()
                                + " " + getString(R.string.is_offline));
                    }
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
            final ImageLoader.ImageListener listener = new ImageLoader.ImageListener() {
                @Override
                public void onResponse(final ImageLoader.ImageContainer response, final boolean isImmediate) {
                    if (response.getBitmap() != null) {
                        addUserPreviewIcon(user, response.getBitmap(), mMap);
                    }
                }

                @Override
                public void onErrorResponse(final VolleyError error) {
                }
            };
            mHTTPManager.asyncLoadBitmap(user.getIconUrl(), listener);
        }
    }

    @Override
    public void onUserUpdated(Integer userId, double lat, double lon) {
        final UserMarker userMarker = mUserMarkers.get(userId);
        if (userMarker != null && lat != Const.BAD_LAT && lon != Const.BAD_LON) {
            userMarker.setVisible(true);
            userMarker.setLatLon(lat, lon);

            final int markerId = userMarker.getMarkerId();
            final ImageView previewIcon = (ImageView) mPictureLayout.findViewById(markerId);
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

            final ImageView previewIcon = (ImageView) mPictureLayout.findViewById(userMarker.getMarkerId());
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
            } catch(Exception e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(final List<Address> addresses) {
            super.onPostExecute(addresses);
            if (addresses == null) {
                Log.d("ANESMELOV", "no address");
            } else {
                mAddresses.clear();
                for (final Address address : addresses) {
                    mAddresses.add(address);
                }
                mAddressListAdapter.notifyDataSetChanged();
            }
        }
    }
}
