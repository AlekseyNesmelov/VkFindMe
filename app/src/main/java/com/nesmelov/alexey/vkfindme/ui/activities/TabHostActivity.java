package com.nesmelov.alexey.vkfindme.ui.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.models.StatusModel;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.ui.fragments.MapFragment;
import com.nesmelov.alexey.vkfindme.ui.fragments.SettingsFragment;
import com.nesmelov.alexey.vkfindme.services.GpsService;
import com.nesmelov.alexey.vkfindme.services.UpdateFriendsService;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.ui.views.CustomViewPager;
import com.nesmelov.alexey.vkfindme.utils.CircleTransform;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TabHostActivity extends Activity {
    private static final int PAGE_NUMBER_MAP = 0;
    private static final int PAGE_NUMBER_SETTINGS = 1;

    private static final int PAGE_COUNT = 2;

    private CustomViewPager mViewPager;

    private HTTPManager mHTTPManager;
    private Storage mStorage;

    private MapFragment mMapFragment;

    private final VKAccessTokenTracker mVkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(final VKAccessToken oldToken, final VKAccessToken newToken) {
            if (newToken == null) {
                FindMeApp.showToast(TabHostActivity.this, getString(R.string.needs_authorization));
                final Intent intent = new Intent(TabHostActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mHTTPManager = FindMeApp.getHTTPManager();
        mStorage = FindMeApp.getStorage();

        final ToggleButton refreshFriendsBtn = findViewById(R.id.refreshBtn);
        refreshFriendsBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !mStorage.getRefreshFriends()) {
                FindMeApp.showToast(TabHostActivity.this, getString(R.string.refresh_friends_is_on));
            } else if (!isChecked && mStorage.getRefreshFriends()) {
                FindMeApp.showToast(this, getString(R.string.refresh_friends_is_off));
            }
            mStorage.setRefreshFriends(isChecked);
            startService(new Intent(TabHostActivity.this, UpdateFriendsService.class));
        });
        refreshFriendsBtn.setChecked(mStorage.getRefreshFriends());

        final ToggleButton visibilityBtn = findViewById(R.id.visibleBtn);
        visibilityBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final Integer user = mStorage.getUserVkId();
            if (isChecked) {
                mHTTPManager.showMe(user, mStorage.getUserLat(), mStorage.getUserLon(),
                        new Callback<StatusModel>() {
                            @Override
                            public void onResponse(@NonNull Call<StatusModel> call, @NonNull Response<StatusModel> response) {
                                if (!mStorage.getVisibility()) {
                                    FindMeApp.showToast(TabHostActivity.this, getString(R.string.visibility_true_message));
                                    mStorage.setVisibility(true);
                                }
                                startService(new Intent(TabHostActivity.this, GpsService.class));
                            }

                            @Override
                            public void onFailure(@NonNull Call<StatusModel> call, @NonNull Throwable t) {
                                FindMeApp.showPopUp(TabHostActivity.this, getString(R.string.error_title),
                                        getString(R.string.on_visibility_server_error_message));
                            }
                        });
            } else {
                mHTTPManager.hideMe(user, new Callback<StatusModel>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusModel> call, @NonNull Response<StatusModel> response) {
                        if (mStorage.getVisibility()) {
                            FindMeApp.showToast(TabHostActivity.this, getString(R.string.visibility_false_message));
                            mStorage.setVisibility(false);
                        }
                        startService(new Intent(TabHostActivity.this, GpsService.class));
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusModel> call, @NonNull Throwable t) {
                        FindMeApp.showPopUp(TabHostActivity.this, getString(R.string.error_title),
                                getString(R.string.off_visibility_server_error_message));
                        mStorage.setVisibility(false);
                        startService(new Intent(TabHostActivity.this, GpsService.class));
                    }
                });
            }
        });
        visibilityBtn.setChecked(mStorage.getVisibility());

        mViewPager = findViewById(R.id.main_pager);
        final PagerAdapter pagerAdapter = new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public CharSequence getPageTitle(final int position) {
                switch (position) {
                    case PAGE_NUMBER_MAP:
                        return getString(R.string.text_tab_map);
                    case PAGE_NUMBER_SETTINGS:
                        return getString(R.string.text_tab_settings);
                }
                return null;
            }

            @Override
            public int getCount() {
                return PAGE_COUNT;
            }

            @Override
            public Fragment getItem(final int position) {
                switch (position) {
                    case PAGE_NUMBER_MAP:
                        mMapFragment = new MapFragment();
                        return mMapFragment;
                    case PAGE_NUMBER_SETTINGS:
                        return new SettingsFragment();
                }
                return null;
            }
        };
        mViewPager.setAdapter(pagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                mViewPager.setPagingEnabled(position == PAGE_NUMBER_SETTINGS);
            }

            @Override
            public void onPageScrolled(final int position, final float positionOffset,
                                       final int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
            }
        });

        final ImageView avatarView = findViewById(R.id.avatar);
        Picasso.with(this)
                .load(mStorage.getUserIconUrl())
                .placeholder(R.drawable.default_user_icon)
                .error(R.drawable.default_user_icon)
                .transform(new CircleTransform())
                .into(avatarView);

        final Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            mHTTPManager.hideMe(mStorage.getUserVkId(), new Callback<StatusModel>() {
                @Override
                public void onResponse(@NonNull Call<StatusModel> call, @NonNull Response<StatusModel> response) {
                    returnToMainActivity();
                }

                @Override
                public void onFailure(@NonNull Call<StatusModel> call, @NonNull Throwable t) {
                    returnToMainActivity();
                }
            });
        });

        mViewPager.setPagingEnabled(false);
        mViewPager.setCurrentItem(PAGE_NUMBER_MAP);

        mVkAccessTokenTracker.startTracking();
    }

    /**
     * Returns to main menu.
     */
    private void returnToMainActivity() {
        stopService(new Intent(TabHostActivity.this, GpsService.class));
        stopService(new Intent(TabHostActivity.this, UpdateFriendsService.class));
        VKSdk.logout();
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
