package com.nesmelov.alexey.vkfindme.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.ui.markers.UserMarker;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * User previews list adapter.
 */
public class UserPreviewsAdapter extends RecyclerView.Adapter<UserPreviewsAdapter.UserPreviewViewHolder> {

    /**
     * User preview clicked listener interface.
     */
    public interface OnUserPreviewClickedListener {
        /**
         * User preview clicked event.
         *
         * @param userMarker clicked user.
         */
        void onUserPreviewClicked(final UserMarker userMarker);
    }

    private final List<UserMarker> mUsers;
    private final Activity mContext;
    private final OnUserPreviewClickedListener mListener;

    /**
     * Constructs users list adapter.
     *
     * @param context context to use.
     * @param users users list.
     */
    public UserPreviewsAdapter(final Activity context,
                               final List<UserMarker> users, final OnUserPreviewClickedListener listener) {
        mUsers = users;
        mContext = context;
        mListener = listener;
    }

    @Override
    public UserPreviewViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_preview, parent, false);
        return new UserPreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserPreviewViewHolder holder, final int position) {
        Picasso.with(mContext)
                .load(mUsers.get(holder.getAdapterPosition()).getIconUrl())
                .placeholder(R.drawable.default_user_icon)
                .error(R.drawable.default_user_icon)
                .into(holder.mIconView);
        holder.mNameView.setText(mUsers.get(holder.getAdapterPosition()).getName());
        holder.mOnlineView.setImageDrawable(mUsers.get(holder.getAdapterPosition()).isVisible()
                ? mContext.getResources().getDrawable(R.drawable.online)
                : mContext.getResources().getDrawable(R.drawable.offline));
        holder.mIconView.setOnClickListener(
                v -> mListener.onUserPreviewClicked(mUsers.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    /**
     * View holder class for user previews list.
     */
    static class UserPreviewViewHolder extends RecyclerView.ViewHolder{
        private ImageView mIconView;
        private TextView mNameView;
        private ImageView mOnlineView;

        /**
         * View holder constructor.
         *
         * @param itemView view of view holder.
         */
        UserPreviewViewHolder(final View itemView) {
            super(itemView);
            mIconView = itemView.findViewById(R.id.icon);
            mNameView = itemView.findViewById(R.id.name);
            mOnlineView = itemView.findViewById(R.id.online);
        }
    }
}
