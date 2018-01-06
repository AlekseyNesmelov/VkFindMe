package com.nesmelov.alexey.vkfindme.ui.adapters;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nesmelov.alexey.vkfindme.R;
import com.nesmelov.alexey.vkfindme.ui.markers.AlarmMarker;

import java.util.List;

/**
 * Alarm previews list adapter.
 */
public class AlarmPreviewsAdapter extends RecyclerView.Adapter<AlarmPreviewsAdapter.AlarmPreviewViewHolder> {

    /**
     * Alarm preview clicked listener interface.
     */
    public interface OnAlarmPreviewClickedListener {
        /**
         * Alarm preview clicked event.
         *
         * @param alarm clicked alarm.
         */
        void onAlarmPreviewClicked(final AlarmMarker alarm);
    }

    private final List<AlarmMarker> mAlarms;
    private final OnAlarmPreviewClickedListener mListener;

    /**
     * Constructs alarms list adapter.
     *
     * @param alarms alarms list.
     * @param listener alarm clicked listener.
     */
    public AlarmPreviewsAdapter(final List<AlarmMarker> alarms,
                                final OnAlarmPreviewClickedListener listener) {
        mAlarms = alarms;
        mListener = listener;
    }

    @Override
    public AlarmPreviewViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_preview, parent, false);
        return new AlarmPreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlarmPreviewViewHolder holder, final int position) {
        holder.mMainView.setBackgroundColor(mAlarms.get(holder.getAdapterPosition()).getColor());
        holder.mMainView.setOnClickListener(
                v -> mListener.onAlarmPreviewClicked(mAlarms.get(holder.getAdapterPosition())));
    }

    @Override
    public int getItemCount() {
        return mAlarms.size();
    }

    /**
     * View holder class for alarm previews list.
     */
    static class AlarmPreviewViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout mMainView;

        /**
         * View holder constructor.
         *
         * @param itemView view of view holder.
         */
        AlarmPreviewViewHolder(final View itemView) {
            super(itemView);
            mMainView = itemView.findViewById(R.id.main);
        }
    }
}
