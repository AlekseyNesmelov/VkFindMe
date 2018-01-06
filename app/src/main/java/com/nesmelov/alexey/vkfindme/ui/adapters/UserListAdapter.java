package com.nesmelov.alexey.vkfindme.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.ui.markers.UserMarker;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Users list adapter.
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private final Activity mContext;
    private final List<UserMarker> mUsers;
    private final List<UserMarker> mCheckedUsers;


    /**
     * Constructs users list adapter.
     *
     * @param context context to use.
     * @param users users list.
     * @param checkedUsers list of checked users.
     */
    public UserListAdapter(final Activity context, final List<UserMarker> users,
                           final List<UserMarker> checkedUsers) {
        mContext = context;
        mUsers = users;
        mCheckedUsers = checkedUsers;
    }

    @Override
    public UserViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, final int position) {
        final UserMarker user = mUsers.get(holder.getAdapterPosition());
        Picasso.with(mContext)
                .load(user.getIconUrl())
                .placeholder(R.drawable.default_user_icon)
                .error(R.drawable.default_user_icon)
                .into(holder.mIconView);
        holder.mNameView.setText(
                String.format(mContext.getString(R.string.user_name_format), user.getName(), user.getSurname()));
        holder.mCheckBox.setChecked(mCheckedUsers.contains(user));
        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!mCheckedUsers.contains(mUsers.get(holder.getAdapterPosition()))) {
                    mCheckedUsers.add(mUsers.get(holder.getAdapterPosition()));
                }
            } else {
                mCheckedUsers.remove(mUsers.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    /**
     * View holder class for users list.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder{
        private ImageView mIconView;
        private TextView mNameView;
        private CheckBox mCheckBox;

        /**
         * View holder constructor.
         *
         * @param itemView view of view holder.
         */
        UserViewHolder(final View itemView) {
            super(itemView);
            mIconView = itemView.findViewById(R.id.icon);
            mNameView = itemView.findViewById(R.id.name);
            mCheckBox = itemView.findViewById(R.id.check_box);
        }
    }
}
