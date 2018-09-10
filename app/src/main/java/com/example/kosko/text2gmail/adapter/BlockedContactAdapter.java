package com.example.kosko.text2gmail.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.database.entity.BlockedContact;

import java.util.HashMap;
import java.util.List;

public class BlockedContactAdapter extends RecyclerView.Adapter<BlockedContactAdapter.ViewHolder> {

    private HashMap<String, String> contactNameMap;
    private List<BlockedContact> blockedContacts;

    public BlockedContactAdapter(List<BlockedContact> blockedContacts, HashMap<String, String> contactNameMap) {
        this.blockedContacts = blockedContacts;
        this.contactNameMap = contactNameMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlockedContact contact = blockedContacts.get(position);
        String contactName = contactNameMap.get(contact.getBlockedNumber());
        holder.getTextViewPhoneNumber().setText(contact.getBlockedNumber());
        holder.getTextViewContactName().setText(contactName == null ? "Unknown" : contactName);
    }

    @Override
    public int getItemCount() {
        return blockedContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewPhoneNumber;
        private TextView textViewContactName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
            textViewContactName = itemView.findViewById(R.id.textViewContactName);
        }

        public TextView getTextViewPhoneNumber() {
            return textViewPhoneNumber;
        }

        public void setTextViewPhoneNumber(TextView textViewPhoneNumber) {
            this.textViewPhoneNumber = textViewPhoneNumber;
        }

        public TextView getTextViewContactName() {
            return textViewContactName;
        }

        public void setTextViewContactName(TextView textViewContactName) {
            this.textViewContactName = textViewContactName;
        }
    }

}
