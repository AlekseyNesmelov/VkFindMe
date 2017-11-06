package com.nesmelov.alexey.vkfindme.ui;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.application.FindMeApp;
import com.nesmelov.alexey.vkfindme.network.HTTPManager;
import com.nesmelov.alexey.vkfindme.structures.User;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Users list adapter.
 */
public class UserListAdapter extends ArrayAdapter<User> {

    private final List<User> mUsers;
    private final Activity mContext;
    private final HTTPManager mHTTPManager;

    /**
     * Constructs users list adapter.
     *
     * @param context context to use.
     * @param users users list.
     */
    public UserListAdapter(final Activity context, final List<User> users) {
        super(context, R.layout.user_list_row, users);
        mUsers = users;
        mContext = context;
        mHTTPManager = FindMeApp.getHTTPManager();
    }

    /**
     * View holder subclass.
     */
    static class ViewHolder {
        protected LinearLayout layout;
        protected ImageView iconView;
        protected TextView nameView;
        protected CheckBox checkBox;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(R.layout.user_list_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.layout = convertView.findViewById(R.id.layout);
            viewHolder.iconView = convertView.findViewById(R.id.icon);

            mHTTPManager.loadImage(mUsers.get(position).getIconUrl(),
                    new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            final okhttp3.ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                mContext.runOnUiThread(() -> {
                                    try {
                                        viewHolder.iconView.setImageBitmap(
                                                BitmapFactory.decodeByteArray(responseBody.bytes(),
                                                0, responseBody.bytes().length));
                                    } catch (Exception ignored) {
                                    }
                                });
                            }
                        }
                    });
            viewHolder.nameView = convertView.findViewById(R.id.name);
            viewHolder.checkBox = convertView.findViewById(R.id.check_box);
            viewHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int getPosition = (Integer) buttonView.getTag();
                mUsers.get(getPosition).setChecked(buttonView.isChecked());
            });
            viewHolder.layout.setOnClickListener(view -> viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked()));
            convertView.setTag(viewHolder);
            convertView.setTag(R.id.name, viewHolder.nameView);
            convertView.setTag(R.id.check_box, viewHolder.checkBox);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.checkBox.setTag(position);

        viewHolder.nameView.setText(mContext.getString(R.string.user_name_format,
                mUsers.get(position).getName(), mUsers.get(position).getSurname()));
        viewHolder.checkBox.setChecked(mUsers.get(position).getChecked());

        return convertView;
    }
}
