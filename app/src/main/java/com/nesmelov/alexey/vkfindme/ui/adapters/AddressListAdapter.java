package com.nesmelov.alexey.vkfindme.ui.adapters;

import android.location.Address;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nesmelov.alexey.vkfindme.R;

import java.util.List;

/**
 * Addresses list adapter.
 */
public class AddressListAdapter extends RecyclerView.Adapter<AddressListAdapter.AddressViewHolder> {

    /**
     * Address clicked listener.
     */
    public interface OnAddressClickedListener {
        /**
         * Address clicked event.
         *
         * @param address clicked address.
         */
        void onAddressClicked(final Address address);
    }

    private final List<Address> mAddresses;
    private OnAddressClickedListener mListener;

    /**
     * Constructs addresses list adapter.
     *
     * @param addresses addresses list.
     * @param listener address clicked listener.
     */
    public AddressListAdapter(final List<Address> addresses, final OnAddressClickedListener listener) {
        mAddresses = addresses;
        mListener = listener;
    }

    @Override
    public AddressViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.address_row, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {
        final Address address = mAddresses.get(holder.getAdapterPosition());

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
            sb.append(address.getAddressLine(i)).append('\n');
        }
        sb.append(address.getAddressLine(address.getMaxAddressLineIndex()));

        holder.mNameView.setText(sb.toString());
        holder.mLayout.setOnClickListener(v -> {
            mListener.onAddressClicked(mAddresses.get(holder.getAdapterPosition()));
        });
    }

    @Override
    public int getItemCount() {
        return mAddresses.size();
    }

    /**
     * View holder class for addresses list.
     */
    static class AddressViewHolder extends RecyclerView.ViewHolder{
        private TextView mNameView;
        private ConstraintLayout mLayout;

        /**
         * View holder constructor.
         *
         * @param itemView view of view holder.
         */
        AddressViewHolder(final View itemView) {
            super(itemView);
            mNameView = itemView.findViewById(R.id.name);
            mLayout = itemView.findViewById(R.id.main);
        }
    }
}
