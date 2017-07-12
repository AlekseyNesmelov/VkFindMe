package com.nesmelov.alexey.vkfindme.pages;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.activities.MainActivity;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import com.nesmelov.alexey.vkfindme.storage.Storage;
import com.vk.sdk.VKSdk;

import org.json.JSONObject;

public class ProfileFragment extends Fragment implements OnUpdateListener {

    private NetworkImageView mAvatarView;
    private TextView mUserNameView;
    private Button mLogoutBtn;

    private Storage mStorage;
    private HTTPManager mHTTPManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorage = FindMeApp.getStorage();
        mHTTPManager = FindMeApp.getHTTPManager();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.profile_page, null);

        mAvatarView = (NetworkImageView) view.findViewById(R.id.avatar);
        mAvatarView.setImageUrl(mStorage.getUserIconUrl(), mHTTPManager.getCircleImageLoader());

        mUserNameView = (TextView) view.findViewById(R.id.user_name);
        mUserNameView.setText(mStorage.getUserName() + " " + mStorage.getUserSurname());

        mLogoutBtn = (Button) view.findViewById(R.id.logoutBtn);
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mHTTPManager.executeRequest(HTTPManager.REQUEST_SET_VISIBILITY_FALSE,
                        HTTPManager.REQUEST_SET_VISIBILITY_TRUE,
                        ProfileFragment.this, mStorage.getUserName());
            }
        });

        return view;
    }

    @Override
    public void onUpdate(final int request, final JSONObject update) {
        returnToMainActivity();
    }

    @Override
    public void onError(final int request, final int errorCode) {
        returnToMainActivity();
    }

    private void returnToMainActivity() {
        VKSdk.logout();
        final Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
