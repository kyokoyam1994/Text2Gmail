package com.example.kosko.text2gmail.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.database.entity.BlockedContact;

import java.util.HashMap;
import java.util.List;

public class BlockedContactAdapter extends ArrayAdapter<BlockedContact> {

    private HashMap<String, String> contactNameMap;

    public BlockedContactAdapter(@NonNull Context context, int resource, @NonNull List<BlockedContact> entries, HashMap<String, String> contactNameMap) {
        super(context, resource, entries);
        this.contactNameMap = contactNameMap;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if(v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.contact_list_item, parent, false);
        }

        BlockedContact contact = getItem(position);
        TextView textViewPhoneNumber = v.findViewById(R.id.textViewPhoneNumber);
        TextView textViewContactName = v.findViewById(R.id.textViewContactName);

        String contactName = contactNameMap.get(contact.getBlockedNumber());
        textViewPhoneNumber.setText(contact.getBlockedNumber());
        textViewContactName.setText(contactName == null ? "Unknown" : contactName);
        return v;
    }

}
