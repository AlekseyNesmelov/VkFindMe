package com.nesmelov.alexey.vkfindme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

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

    private ListView mUsersListView;
    private List<User> mUsers;
    private UserListAdapter mUserListAdapter;
    private Storage mStorage;

    private ImageButton mOkBtn;
    private ImageButton mNokBtn;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_users_activity);

        mUsersListView = (ListView) findViewById(R.id.usersList);

        mStorage = FindMeApp.getStorage();
        mUsers = new CopyOnWriteArrayList<>();

        mUserListAdapter = new UserListAdapter(this, mUsers);
        mUsersListView.setAdapter(mUserListAdapter);

        mOkBtn = (ImageButton) findViewById(R.id.okBtn);
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<Integer> checkedUsers = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                for (final User user : mUsers) {
                    if (user.getChecked()) {
                        checkedUsers.add(user.getVkId());
                        sb.append(user.getName() + " " + user.getSurname()).append(", ");
                    }
                }
                if (sb.length() != 0) {
                    sb = sb.delete(sb.length() - 2, sb.length());
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
                    intent.putExtra(Const.NAMES, sb.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        mNokBtn = (ImageButton) findViewById(R.id.nokBtn);
        mNokBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent();
                intent.putExtra(Const.ALARM_ID, getIntent().getLongExtra(Const.ALARM_ID, Const.BAD_ID));
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        final User user = new User();
        user.setName(mStorage.getUserName());
        user.setSurname(mStorage.getUserSurname());
        user.setIconUrl(mStorage.getUserIconUrl());
        user.setVkId(mStorage.getUserVkId());
        mUsers.add(user);

        mUsers.addAll(mStorage.getFriends(Const.FRIENDS_LIMIT));

        if (getIntent().getLongExtra(Const.ALARM_ID, Const.BAD_ID) != Const.BAD_ID) {
            final ArrayList<Integer> checkedUsers = getIntent().getIntegerArrayListExtra(Const.USERS);

            for (final User u : mUsers) {
                if (checkedUsers.contains(u.getVkId())) {
                    u.setChecked(true);
                }
            }
        }
        mUserListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, getIntent());
        finish();
    }
}

