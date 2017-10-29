package com.nesmelov.alexey.vkfindme.ui.activities;

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
import com.nesmelov.alexey.vkfindme.storage.Const;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.nesmelov.alexey.vkfindme.structures.User;
import com.nesmelov.alexey.vkfindme.ui.UserListAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

        ListView mUsersListView = findViewById(R.id.usersList);
        mUserListAdapter = new UserListAdapter(this, mUsers);
        mUsersListView.setAdapter(mUserListAdapter);

        final Button okBtn = findViewById(R.id.okBtn);
        okBtn.setOnClickListener(v -> {
            final ArrayList<Integer> checkedUsers = new ArrayList<>();
            StringBuilder userNames = new StringBuilder();
            for (final User user : mUsers) {
                if (user.getChecked()) {
                    checkedUsers.add(user.getVkId());
                    userNames.append(user.getName() + " " + user.getSurname()).append(", ");
                }
            }
            if (userNames.length() != 0) {
                userNames = userNames.delete(userNames.length() - 2, userNames.length());
            }

            if (checkedUsers.isEmpty()) {
                FindMeApp.showToast(AlarmUsersActivity.this, getString(R.string.alarm_must_select_users_message));
            } else {
                final Intent intent = new Intent();
                intent.putExtra(Const.ALARM_ID, getIntent().getLongExtra(Const.ALARM_ID, Const.BAD_ID));
                intent.putExtra(Const.LAT, getIntent().getDoubleExtra(Const.LAT, Const.BAD_LAT));
                intent.putExtra(Const.LON, getIntent().getDoubleExtra(Const.LON, Const.BAD_LON));
                intent.putExtra(Const.RADIUS, getIntent().getFloatExtra(Const.RADIUS, Const.BAD_RADIUS));
                intent.putIntegerArrayListExtra(Const.USERS, checkedUsers);
                intent.putExtra(Const.NAMES, userNames.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        final Button nokBtn = findViewById(R.id.nokBtn);
        nokBtn.setOnClickListener(v -> {
            final Intent intent = new Intent();
            intent.putExtra(Const.ALARM_ID, getIntent().getLongExtra(Const.ALARM_ID, Const.BAD_ID));
            setResult(Const.RESULT_UPDATE, intent);
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

    private class LoadUsersTask extends AsyncTask<Void, Void, List<User>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<User> doInBackground(Void... params) {
            final List<User> users = mStorage.getFriends(Const.FRIENDS_LIMIT);

            if (getIntent().getLongExtra(Const.ALARM_ID, Const.BAD_ID) != Const.BAD_ID) {
                final ArrayList<Integer> checkedUsers = getIntent().getIntegerArrayListExtra(Const.USERS);

                for (final User user : users) {
                    if (checkedUsers.contains(user.getVkId())) {
                        user.setChecked(true);
                    }
                }

                mUser.setChecked(checkedUsers.contains(mUser.getVkId()));
            }

            return users;
        }

        @Override
        protected void onPostExecute(final List<User> users) {
            super.onPostExecute(users);
            mUsers.add(mUser);
            mUsers.addAll(users);
            mUserListAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
        }
    }
}

