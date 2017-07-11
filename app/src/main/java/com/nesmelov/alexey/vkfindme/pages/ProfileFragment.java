package com.nesmelov.alexey.vkfindme.pages;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.network.OnUpdateListener;
import org.json.JSONObject;

public class ProfileFragment extends Fragment implements OnUpdateListener{

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.profile_page, null);

        return view;
    }

    @Override
    public void onUpdate(int request, JSONObject update) {

    }

    @Override
    public void onError(int request, int errorCode) {

    }
}
