package com.nesmelov.alexey.vkfindme.ui;

import android.app.Activity;
import android.location.Address;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nesmelov.alexey.vkfindme.R;
import java.util.List;

/**
 * Address list adapter.
 */
public class AddressListAdapter extends ArrayAdapter<Address> {

    private final List<Address> mAddresses;
    private final Activity mContext;

    /**
     * Address list adapter.
     *
     * @param context context to use.
     * @param addresses list of addresses.
     */
    public AddressListAdapter(final Activity context, final List<Address> addresses) {
        super(context, R.layout.address_row, addresses);
        mAddresses = addresses;
        mContext = context;
    }

    /**
     * View holder class.
     */
    static class ViewHolder {
        protected LinearLayout layout;
        protected TextView nameView;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(R.layout.address_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.layout = convertView.findViewById(R.id.layout);
            viewHolder.nameView = convertView.findViewById(R.id.name);
            convertView.setTag(viewHolder);
            convertView.setTag(R.id.name, viewHolder.nameView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.nameView.setText(mAddresses.get(position).getAddressLine(0));

        return convertView;
    }
}
