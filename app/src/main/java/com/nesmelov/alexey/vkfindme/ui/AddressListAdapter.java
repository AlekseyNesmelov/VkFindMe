package com.nesmelov.alexey.vkfindme.ui;

import android.app.Activity;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nesmelov.alexey.vkfindme.R;
import java.util.List;

public class AddressListAdapter extends ArrayAdapter<Address> {

    private final List<Address> mAddresses;
    private final Activity mContext;

    public AddressListAdapter(final Activity context, final List<Address> addresses) {
        super(context, R.layout.address_row, addresses);
        mAddresses = addresses;
        mContext = context;
    }

    static class ViewHolder {
        protected LinearLayout layout;
        protected TextView nameView;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(R.layout.address_row, null);
            viewHolder = new ViewHolder();
            viewHolder.layout = (LinearLayout) convertView.findViewById(R.id.layout);
            viewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(viewHolder);
            convertView.setTag(R.id.name, viewHolder.nameView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(mAddresses.get(position).getAddressLine(0));
        viewHolder.nameView.setText(sb.toString());

        return convertView;
    }
}
