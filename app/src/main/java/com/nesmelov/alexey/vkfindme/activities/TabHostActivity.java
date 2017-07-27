package com.nesmelov.alexey.vkfindme.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
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
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import com.nesmelov.alexey.vkfindme.pages.MapFragment;
import com.nesmelov.alexey.vkfindme.pages.SettingsFragment;
import com.nesmelov.alexey.vkfindme.services.GpsService;
import com.nesmelov.alexey.vkfindme.services.UpdateFriendsService;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.ui.CircleImageView;
import com.nesmelov.alexey.vkfindme.ui.CustomViewPager;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import org.json.JSONObject;

public class TabHostActivity extends Activity implements OnUpdateListener{
    private static final int PAGE_NUMBER_MAP = 0;
    private static final int PAGE_NUMBER_SETTINGS = 1;

    private static final int PAGE_COUNT = 2;

    private ProgressBar mProgressBar;
    private CustomViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    private CircleImageView mAvatarView;
    private Button mLogoutBtn;

    private HTTPManager mHTTPManager;
    private Storage mStorage;

    private ToggleButton mRefreshFriendsBtn;

    private ToggleButton mVisibilityBtn;
    private CompoundButton.OnCheckedChangeListener mVisibilityBtnListener;

    private boolean mIsLogout = false;

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

        mRefreshFriendsBtn = (ToggleButton) findViewById(R.id.refreshBtn);
        mRefreshFriendsBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FindMeApp.showToast(TabHostActivity.this, getString(R.string.refresh_friends_is_on));
                }
                mStorage.setRefreshFriends(isChecked);
                startService(new Intent(TabHostActivity.this, UpdateFriendsService.class));
            }
        });
        mRefreshFriendsBtn.setChecked(mStorage.getRefreshFriends());

        mVisibilityBtn = (ToggleButton) findViewById(R.id.visibleBtn);
        mVisibilityBtnListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Integer user = mStorage.getUserVkId();
                if (isChecked) {
                    mHTTPManager.executeRequest(HTTPManager.REQUEST_SET_VISIBILITY_TRUE,
                            HTTPManager.REQUEST_SET_VISIBILITY_FALSE,
                            TabHostActivity.this, user.toString(),
                            String.valueOf(mStorage.getUserLat()),
                            String.valueOf(mStorage.getUserLon()));
                } else {
                    mHTTPManager.executeRequest(HTTPManager.REQUEST_SET_VISIBILITY_FALSE,
                            HTTPManager.REQUEST_SET_VISIBILITY_TRUE,
                            TabHostActivity.this, user.toString());
                }
            }
        };
        mVisibilityBtn.setChecked(mStorage.getVisibility());
        mVisibilityBtn.setOnCheckedChangeListener(mVisibilityBtnListener);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mViewPager = (CustomViewPager) findViewById(R.id.main_pager);
        mPagerAdapter = new FragmentPagerAdapter(getFragmentManager()) {
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
                        return new MapFragment();
                    case PAGE_NUMBER_SETTINGS:
                        return new SettingsFragment();
                }
                return null;
            }
        };
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                switch (position) {
                    case PAGE_NUMBER_MAP:
                        mViewPager.setPagingEnabled(false);
                        break;
                    case PAGE_NUMBER_SETTINGS:
                        mViewPager.setPagingEnabled(true);
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

        mAvatarView = (CircleImageView) findViewById(R.id.avatar);
        mHTTPManager.asyncLoadBitmap(mStorage.getUserIconUrl(), mAvatarView);

        mLogoutBtn = (Button) findViewById(R.id.logoutBtn);
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mIsLogout = true;
                mHTTPManager.executeRequest(HTTPManager.REQUEST_SET_VISIBILITY_FALSE,
                        HTTPManager.REQUEST_SET_VISIBILITY_TRUE,
                        TabHostActivity.this, mStorage.getUserVkId().toString());
            }
        });

        mViewPager.setCurrentItem(PAGE_NUMBER_MAP);

        mVkAccessTokenTracker.startTracking();
    }

    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onUpdate(int request, JSONObject update) {
        switch (request) {
            case HTTPManager.REQUEST_SET_VISIBILITY_TRUE:
                FindMeApp.showToast(TabHostActivity.this, getString(R.string.visibility_true_message));
                mStorage.setVisibility(true);
                startService(new Intent(TabHostActivity.this, GpsService.class));
                break;
            case HTTPManager.REQUEST_SET_VISIBILITY_FALSE:
                if (mIsLogout) {
                    returnToMainActivity();
                } else {
                    mStorage.setVisibility(false);
                    startService(new Intent(TabHostActivity.this, GpsService.class));
                    FindMeApp.showToast(TabHostActivity.this, getString(R.string.visibility_false_message));
                }
                break;
            default:
                returnToMainActivity();
        }
    }

    @Override
    public void onError(int request, int errorCode) {
        switch (request) {
            case HTTPManager.REQUEST_SET_VISIBILITY_TRUE:
                FindMeApp.showPopUp(TabHostActivity.this, getString(R.string.error_title),
                        getString(R.string.on_visibility_server_error_message));
                mVisibilityBtn.setOnCheckedChangeListener(null);
                mVisibilityBtn.setChecked(mStorage.getVisibility());
                mVisibilityBtn.setOnCheckedChangeListener(mVisibilityBtnListener);
                break;
            case HTTPManager.REQUEST_SET_VISIBILITY_FALSE:
                if (mIsLogout) {
                    returnToMainActivity();
                } else {
                    FindMeApp.showPopUp(TabHostActivity.this, getString(R.string.error_title),
                            getString(R.string.off_visibility_server_error_message));
                    mStorage.setVisibility(false);
                    startService(new Intent(TabHostActivity.this, GpsService.class));
                }
                break;
            default:
                returnToMainActivity();
        }
    }

    private void returnToMainActivity() {
        VKSdk.logout();
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
