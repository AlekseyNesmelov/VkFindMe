package com.nesmelov.alexey.vkfindme.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.structures.User;
import com.nesmelov.alexey.vkfindme.ui.UserListAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Activity to manage alarm users.
 */
public class AlarmUsersActivity extends FragmentActivity {
    private List<User> mUsers = new CopyOnWriteArrayList<>();
    private UserListAdapter mUserListAdapter;

    private Storage mStorage;

    private ProgressBar mProgressBar;

    private User mUser = new User();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_users_activity);

        mStorage = FindMeApp.getStorage();

        mProgressBar = findViewById(R.id.progressBar);

        final ListView mUsersListView = findViewById(R.id.usersList);
        mUserListAdapter = new UserListAdapter(this, mUsers);
        mUsersListView.setAdapter(mUserListAdapter);

        final Button okBtn = findViewById(R.id.okBtn);
        okBtn.setOnClickListener(v -> {
            final ArrayList<Integer> checkedUsers = new ArrayList<>();
            StringBuilder userNames = new StringBuilder();
            for (final User user : mUsers) {
                if (user.getChecked()) {
                    checkedUsers.add(user.getVkId());
                    userNames.append(user.getName()).append(" ").append(user.getSurname()).append(", ");
                }
            }
            if (userNames.length() != 0) {
                userNames = userNames.delete(userNames.length() - 2, userNames.length());
            }

            if (checkedUsers.isEmpty()) {
                FindMeApp.showToast(AlarmUsersActivity.this, getString(R.string.alarm_must_select_users_message));
            } else {
                final Intent intent = new Intent();
                intent.putExtra(Storage.ALARM_ID, getIntent().getLongExtra(Storage.ALARM_ID, Storage.BAD_ID));
                intent.putExtra(Storage.LAT, getIntent().getDoubleExtra(Storage.LAT, Storage.BAD_LAT));
                intent.putExtra(Storage.LON, getIntent().getDoubleExtra(Storage.LON, Storage.BAD_LON));
                intent.putExtra(Storage.RADIUS, getIntent().getFloatExtra(Storage.RADIUS, Storage.BAD_RADIUS));
                intent.putIntegerArrayListExtra(Storage.USERS, checkedUsers);
                intent.putExtra(Storage.NAMES, userNames.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        final Button nokBtn = findViewById(R.id.nokBtn);
        nokBtn.setOnClickListener(v -> {
            final Intent intent = new Intent();
            intent.putExtra(Storage.ALARM_ID, getIntent().getLongExtra(Storage.ALARM_ID, Storage.BAD_ID));
            setResult(Storage.RESULT_UPDATE, intent);
            finish();
        });

        mUser.setName(mStorage.getUserName());
        mUser.setSurname(mStorage.getUserSurname());
        mUser.setIconUrl(mStorage.getUserIconUrl());
        mUser.setVkId(mStorage.getUserVkId());

        final LoadUsersTask loadUsersTsk = new LoadUsersTask();
        loadUsersTsk.execute();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, getIntent());
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadUsersTask extends AsyncTask<Void, User, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            final List<User> users = mStorage.getFriends(Storage.FRIENDS_LIMIT);

            if (getIntent().getLongExtra(Storage.ALARM_ID, Storage.BAD_ID) != Storage.BAD_ID) {
                final ArrayList<Integer> checkedUsers = getIntent().getIntegerArrayListExtra(Storage.USERS);
                mUser.setChecked(checkedUsers.contains(mUser.getVkId()));
                for (final User user : users) {
                    if (checkedUsers.contains(user.getVkId())) {
                        user.setChecked(true);
                    }
                }
            }

            publishProgress(mUser);
            for (final User user : users) {
                publishProgress(user);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(User... values) {
            mUsers.add(values[0]);
            mUserListAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(final Void users) {
            super.onPostExecute(users);
            mProgressBar.setVisibility(View.GONE);
        }
    }
}

