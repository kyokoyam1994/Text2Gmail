package com.example.kosko.text2gmail.adapter;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.database.entity.BlockedContact;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlockedContactAdapter extends RecyclerView.Adapter<BlockedContactAdapter.ViewHolder> {

    public interface BlockedContactListener {
        void onBlockedContactDeleted(BlockedContact blockedContact);
    }

    private BlockedContactListener blockedContactListener;
    private List<BlockedContact> blockedContacts;
    private HashMap<String, String> contactNameMap;
    private HashMap<String, String> contactImageMap;

    public BlockedContactAdapter(BlockedContactListener blockedContactListener, List<BlockedContact> blockedContacts, HashMap<String, String> contactNameMap, HashMap<String, String> contactImageMap) {
        this.blockedContactListener = blockedContactListener;
        this.blockedContacts = blockedContacts;
        this.contactNameMap = contactNameMap;
        this.contactImageMap = contactImageMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blocked_contact_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getButtonDeleteBlockedContact().setOnClickListener(holder);
        BlockedContact contact = blockedContacts.get(position);
        String contactName = contactNameMap.get(contact.getBlockedNumber());
        holder.getTextViewPhoneNumber().setText(contact.getBlockedNumber());
        holder.getTextViewContactName().setText(contactName == null ? "Unknown" : contactName);

        String image = contactImageMap.get(contact.getBlockedNumber());
        boolean invalidURI = true;
        if (image != null) {
            try {
                holder.getImageViewContactPhoto().setImageURI(Uri.parse(image));
                invalidURI = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(invalidURI) holder.getImageViewContactPhoto().setImageResource(R.drawable.unknown_user_icon);
    }

    @Override
    public int getItemCount() {
        return blockedContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView textViewPhoneNumber;
        private TextView textViewContactName;
        private ImageButton buttonDeleteBlockedContact;
        private CircleImageView imageViewContactPhoto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
            textViewContactName = itemView.findViewById(R.id.textViewContactName);
            buttonDeleteBlockedContact = itemView.findViewById(R.id.buttonDeleteBlockedContact);
            imageViewContactPhoto = itemView.findViewById(R.id.imageViewContactPhoto);
        }

        public TextView getTextViewPhoneNumber() {
            return textViewPhoneNumber;
        }

        public TextView getTextViewContactName() {
            return textViewContactName;
        }

        public ImageButton getButtonDeleteBlockedContact() {
            return buttonDeleteBlockedContact;
        }

        public CircleImageView getImageViewContactPhoto() {
            return imageViewContactPhoto;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.buttonDeleteBlockedContact:
                    blockedContactListener.onBlockedContactDeleted(blockedContacts.get(getAdapterPosition()));
                    break;
            }
        }
    }

}
