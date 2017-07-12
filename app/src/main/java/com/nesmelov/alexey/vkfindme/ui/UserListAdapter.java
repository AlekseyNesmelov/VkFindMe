package com.nesmelov.alexey.vkfindme.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.structures.User;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> {

    private final List<User> mUsers;
    private final Activity mContext;

    public UserListAdapter(final Activity context, final List<User> users) {
        super(context, R.layout.user_list_row, users);
        mUsers = users;
        mContext = context;
    }

    static class ViewHolder {
        protected ImageView iconView;
        protected TextView nameView;
        protected CheckBox checkBox;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(R.layout.user_list_row, null);
            viewHolder = new ViewHolder();
            viewHolder.iconView = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    mUsers.get(getPosition).setChecked(buttonView.isChecked()); // Set the value of checkbox to maintain its state.
                }
            });
            convertView.setTag(viewHolder);
            convertView.setTag(R.id.name, viewHolder.nameView);
            convertView.setTag(R.id.check_box, viewHolder.checkBox);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.checkBox.setTag(position); // This line is important.

        viewHolder.nameView.setText(mUsers.get(position).getName() + " " + mUsers.get(position).getSurname());
        viewHolder.checkBox.setChecked(mUsers.get(position).getChecked());

        return convertView;
    }
}