package com.nesmelov.alexey.vkfindme.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.pages.MapFragment;
import com.nesmelov.alexey.vkfindme.pages.ProfileFragment;
import com.nesmelov.alexey.vkfindme.pages.SettingsFragment;
import com.nesmelov.alexey.vkfindme.ui.CustomViewPager;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;

public class TabHostActivity extends Activity {
    private static final String TAG_PROFILE = "tagProfile";
    private static final String TAG_MAP = "tagMap";
    private static final String TAG_SETTINGS = "tagSettings";

    private static final int PAGE_NUMBER_PROFILE = 0;
    private static final int PAGE_NUMBER_MAP = 1;
    private static final int PAGE_NUMBER_SETTINGS = 2;

    private static final int PAGE_COUNT = 3;

    private ProgressBar mProgressBar;
    private CustomViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private TabHost mTabHost;

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

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mViewPager = (CustomViewPager) findViewById(R.id.main_pager);
        mPagerAdapter = new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public int getCount() {
                return PAGE_COUNT;
            }

            @Override
            public Fragment getItem(final int position) {
                switch (position) {
                    case PAGE_NUMBER_PROFILE:
                        return new ProfileFragment();
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
                    case PAGE_NUMBER_PROFILE:
                        mTabHost.setCurrentTabByTag(TAG_PROFILE);
                        mViewPager.setPagingEnabled(true);
                        break;
                    case PAGE_NUMBER_MAP:
                        mTabHost.setCurrentTabByTag(TAG_MAP);
                        mViewPager.setPagingEnabled(false);
                        break;
                    case PAGE_NUMBER_SETTINGS:
                        mTabHost.setCurrentTabByTag(TAG_SETTINGS);
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

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabHost.getTabWidget().setDividerDrawable(R.drawable.tabhost_divider);
        setupTab(TAG_PROFILE, getResources().getString(R.string.text_tab_profile));
        setupTab(TAG_MAP, getResources().getString(R.string.text_tab_map));
        setupTab(TAG_SETTINGS, getResources().getString(R.string.text_tab_settings));

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(final String tabTag) {
                switch (tabTag) {
                    case TAG_PROFILE:
                        mViewPager.setCurrentItem(PAGE_NUMBER_PROFILE);
                        mViewPager.setPagingEnabled(true);
                        break;
                    case TAG_MAP:
                        mViewPager.setCurrentItem(PAGE_NUMBER_MAP);
                        mViewPager.setPagingEnabled(false);
                        break;
                    case TAG_SETTINGS:
                        mViewPager.setCurrentItem(PAGE_NUMBER_SETTINGS);
                        mViewPager.setPagingEnabled(true);
                        break;
                }
            }
        });

        mViewPager.setCurrentItem(PAGE_NUMBER_MAP);
        mTabHost.setCurrentTabByTag(TAG_MAP);

        mVkAccessTokenTracker.startTracking();
    }

    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void setupTab(final String tag, final String name) {
        final View tabView = createTabView(mTabHost.getContext(), name);
        final TabHost.TabSpec setContent = mTabHost.newTabSpec(tag)
                .setIndicator(tabView).setContent(new TabHost.TabContentFactory() {
            public View createTabContent(final String tag) {
                return  new TextView(TabHostActivity.this);
            }
        });
        mTabHost.addTab(setContent);
    }

    private View createTabView(final Context context, final String text) {
        final View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
        final TextView textView = (TextView) view.findViewById(R.id.tabsText);
        textView.setText(text);
        return view;
    }
}
