package com.nesmelov.alexey.vkfindme.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.tasks.LoadUsersListTask;
import com.nesmelov.alexey.vkfindme.ui.adapters.UserListAdapter;
import com.nesmelov.alexey.vkfindme.ui.markers.UserMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to manage alarm users.
 */
public class AlarmUsersActivity extends FragmentActivity implements LoadUsersListTask.OnLoadUsersListener {

    private static final String BUNDLE_CHECKED_USERS = "checked_users";

    private List<UserMarker> mUsers;
    private List<UserMarker> mCheckedUsers;
    private UserListAdapter mUserListAdapter;

    private ProgressBar mProgressBar;

    private LoadUsersListTask mLoadUsersListTask;

    private ArrayList<Integer> mIntentCheckedUsers;
    private ArrayList<Integer> mBundleCheckedUsers;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_users_activity);

        mUsers = new ArrayList<>();
        mCheckedUsers = new ArrayList<>();

        mProgressBar = findViewById(R.id.progressBar);

        final RecyclerView usersListView = findViewById(R.id.usersList);
        usersListView.setLayoutManager(new LinearLayoutManager(this));
        mUserListAdapter = new UserListAdapter(this, mUsers, mCheckedUsers);
        usersListView.setAdapter(mUserListAdapter);

        final Button okBtn = findViewById(R.id.okBtn);
        okBtn.setOnClickListener(v -> {
            if (mCheckedUsers.isEmpty()) {
                FindMeApp.showToast(AlarmUsersActivity.this, getString(R.string.alarm_must_select_users_message));
            } else {
                final StringBuilder userNames = new StringBuilder();
                final ArrayList<Integer> checkedIds = new ArrayList<>();
                for (int i = 0; i < mCheckedUsers.size() - 1; i++) {
                    final UserMarker user = mCheckedUsers.get(i);
                    checkedIds.add(user.getVkId());
                    userNames.append(user.getName()).append(" ").append(user.getSurname()).append(", ");
                }
                final UserMarker lastUser = mCheckedUsers.get(mCheckedUsers.size() - 1);
                checkedIds.add(lastUser.getVkId());
                userNames.append(lastUser.getName()).append(" ").append(lastUser.getSurname());

                final Intent intent = new Intent();
                intent.putExtra(Storage.ALARM_ID, getIntent().getIntExtra(Storage.ALARM_ID, Storage.BAD_ID));
                intent.putExtra(Storage.LAT, getIntent().getDoubleExtra(Storage.LAT, Storage.BAD_LAT));
                intent.putExtra(Storage.LON, getIntent().getDoubleExtra(Storage.LON, Storage.BAD_LON));
                intent.putExtra(Storage.RADIUS, getIntent().getFloatExtra(Storage.RADIUS, Storage.BAD_RADIUS));
                intent.putIntegerArrayListExtra(Storage.USERS, checkedIds);
                intent.putExtra(Storage.NAMES, userNames.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        final Button nokBtn = findViewById(R.id.nokBtn);
        nokBtn.setOnClickListener(v -> {
            final Intent intent = new Intent();
            intent.putExtra(Storage.ALARM_ID, getIntent().getIntExtra(Storage.ALARM_ID, Storage.BAD_ID));
            setResult(Storage.RESULT_REMOVE, intent);
            finish();
        });

        if (savedInstanceState != null) {
            mBundleCheckedUsers = savedInstanceState.getIntegerArrayList(BUNDLE_CHECKED_USERS);
        } else if (getIntent() != null) {
            mIntentCheckedUsers = getIntent().getIntegerArrayListExtra(Storage.USERS);
        }

        if (mLoadUsersListTask != null) {
            mLoadUsersListTask.cancel(true);
        }
        mLoadUsersListTask = new LoadUsersListTask();
        mLoadUsersListTask.setListener(this);
        mLoadUsersListTask.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        final ArrayList<Integer> checkedIds = new ArrayList<>();
        for (final UserMarker user : mCheckedUsers) {
            checkedIds.add(user.getVkId());
        }
        outState.putIntegerArrayList(BUNDLE_CHECKED_USERS, checkedIds);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadUsersListTask != null) {
            mLoadUsersListTask.cancel(true);
            mLoadUsersListTask.setListener(null);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, getIntent());
        finish();
    }

    @Override
    public void onLoadStarted() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserLoaded(final UserMarker user) {
        if (mIntentCheckedUsers != null && mIntentCheckedUsers.contains(user.getVkId())
                || mBundleCheckedUsers != null && mBundleCheckedUsers.contains(user.getVkId())) {
           mCheckedUsers.add(user);
        }
        mUsers.add(user);
        mUserListAdapter.notifyItemInserted(mUsers.size() - 1);
    }

    @Override
    public void onLoadCompleted() {
        mProgressBar.setVisibility(View.GONE);
    }
}

