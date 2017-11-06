package com.nesmelov.alexey.vkfindme.ui.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
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
import com.nesmelov.alexey.vkfindme.ui.CircleImageView;
import com.nesmelov.alexey.vkfindme.ui.CustomViewPager;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TabHostActivity extends Activity {
    private static final int PAGE_NUMBER_MAP = 0;
    private static final int PAGE_NUMBER_SETTINGS = 1;

    private static final int PAGE_COUNT = 2;

    private ProgressBar mProgressBar;
    private CustomViewPager mViewPager;

    private HTTPManager mHTTPManager;
    private Storage mStorage;

    private ToggleButton mVisibilityBtn;
    private CompoundButton.OnCheckedChangeListener mVisibilityBtnListener;

    private ToggleButton mShowDrawerBtn;
    private ToggleButton mShowSearchBtn;

    private MapFragment mMapFragment;

    final VKAccessTokenTracker mVkAccessTokenTracker = new VKAccessTokenTracker() {
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
            if (isChecked) {
                FindMeApp.showToast(TabHostActivity.this, getString(R.string.refresh_friends_is_on));
            }
            mStorage.setRefreshFriends(isChecked);
            startService(new Intent(TabHostActivity.this, UpdateFriendsService.class));
        });
        refreshFriendsBtn.setChecked(mStorage.getRefreshFriends());

        mShowDrawerBtn = findViewById(R.id.show_drawer);
        mShowDrawerBtn.setOnCheckedChangeListener((compoundButton, b) -> {
        if (mMapFragment != null) {
            mMapFragment.showAlarms(b);
        }
        });

        mShowSearchBtn = findViewById(R.id.show_searcher);
        mShowSearchBtn.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mMapFragment != null) {
                mMapFragment.showSearchView(b);
            }
        });

        mVisibilityBtn = findViewById(R.id.visibleBtn);
        mVisibilityBtnListener = (buttonView, isChecked) -> {
            final Integer user = mStorage.getUserVkId();
            if (isChecked) {
                mHTTPManager.showMe(user, mStorage.getUserLat(), mStorage.getUserLon(),
                        new Callback<StatusModel>() {
                            @Override
                            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                                FindMeApp.showToast(TabHostActivity.this, getString(R.string.visibility_true_message));
                                mStorage.setVisibility(true);
                                startService(new Intent(TabHostActivity.this, GpsService.class));
                            }

                            @Override
                            public void onFailure(Call<StatusModel> call, Throwable t) {
                                FindMeApp.showPopUp(TabHostActivity.this, getString(R.string.error_title),
                                        getString(R.string.on_visibility_server_error_message));
                                mVisibilityBtn.setOnCheckedChangeListener(null);
                                mVisibilityBtn.setChecked(mStorage.getVisibility());
                                mVisibilityBtn.setOnCheckedChangeListener(mVisibilityBtnListener);
                            }
                        });
            } else {
                mHTTPManager.hideMe(user, new Callback<StatusModel>() {
                    @Override
                    public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                        FindMeApp.showToast(TabHostActivity.this, getString(R.string.visibility_false_message));
                        mStorage.setVisibility(false);
                        startService(new Intent(TabHostActivity.this, GpsService.class));
                    }

                    @Override
                    public void onFailure(Call<StatusModel> call, Throwable t) {
                        FindMeApp.showPopUp(TabHostActivity.this, getString(R.string.error_title),
                                getString(R.string.off_visibility_server_error_message));
                        mStorage.setVisibility(false);
                        startService(new Intent(TabHostActivity.this, GpsService.class));
                    }
                });
            }
        };
        mVisibilityBtn.setOnCheckedChangeListener(mVisibilityBtnListener);
        mVisibilityBtn.setChecked(mStorage.getVisibility());

        mProgressBar = findViewById(R.id.progressBar);
        mViewPager = findViewById(R.id.main_pager);
        final PagerAdapter pagerAdapter = new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case PAGE_NUMBER_MAP:
                        return getString(R.string.text_tab_map);
                    case PAGE_NUMBER_SETTINGS:
                        return getString(R.string.text_tab_settings);
                }
                return "";
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
                switch (position) {
                    case PAGE_NUMBER_MAP:
                        mViewPager.setPagingEnabled(false);
                        mShowDrawerBtn.setVisibility(View.VISIBLE);
                        mShowDrawerBtn.animate().setDuration(500).alpha(1);
                        mShowSearchBtn.setVisibility(View.VISIBLE);
                        mShowSearchBtn.animate().setDuration(500).alpha(1);
                        break;
                    case PAGE_NUMBER_SETTINGS:
                        mViewPager.setPagingEnabled(true);
                        mShowDrawerBtn.setVisibility(View.GONE);
                        mShowDrawerBtn.setAlpha(0);
                        mShowSearchBtn.setVisibility(View.GONE);
                        mShowSearchBtn.setAlpha(0);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrolled(final int position, final float positionOffset,
                                       final int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
            }
        });
        mViewPager.setPagingEnabled(false);

        final CircleImageView avatarView = findViewById(R.id.avatar);
        mHTTPManager.loadImage(mStorage.getUserIconUrl(), new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.body() != null) {
                    final byte[] bytes = response.body().bytes();
                    runOnUiThread(() -> avatarView.setImageBitmap(BitmapFactory.decodeByteArray(bytes,
                            0, bytes.length)));
                }
            }
        });

        final Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            mHTTPManager.hideMe(mStorage.getUserVkId(), new Callback<StatusModel>() {
                @Override
                public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                    returnToMainActivity();
                }

                @Override
                public void onFailure(Call<StatusModel> call, Throwable t) {
                    returnToMainActivity();
                }
            });
        });

        mViewPager.setCurrentItem(PAGE_NUMBER_MAP);

        mVkAccessTokenTracker.startTracking();
    }

    public void clickSearchButton() {
        mShowSearchBtn.toggle();
    }

    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    public void setCheckedShowDrawerBtn(final boolean checked){
        mShowDrawerBtn.setChecked(checked);
    }

    public void setCheckedShowSearchBtn(final boolean checked){
        mShowSearchBtn.setChecked(checked);
    }

    private void returnToMainActivity() {
        stopService(new Intent(TabHostActivity.this, GpsService.class));
        stopService(new Intent(TabHostActivity.this, UpdateFriendsService.class));
        VKSdk.logout();
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
