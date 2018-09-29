package com.example.kosko.text2gmail.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.kosko.text2gmail.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactSelectionAdapter extends CursorAdapter implements View.OnClickListener {

    private ContactListener contactListener;

    public interface ContactListener {
        ArrayList<String> getContacts();
        void onContactAdded(String contactNumber);
    }

    public ContactSelectionAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        try {
            contactListener = (ContactListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " must implement" + ContactListener.class.toString());
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.contact_list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textViewContactName = view.findViewById(R.id.textViewContactName);
        TextView textViewPhoneNumber = view.findViewById(R.id.textViewPhoneNumber);
        CircleImageView imageViewContactPhoto = view.findViewById(R.id.imageViewContactPhoto);
        Button buttonAddContact = view.findViewById(R.id.buttonAddContact);
        buttonAddContact.setOnClickListener(this);

        if (contactListener.getContacts().contains(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))) {
            buttonAddContact.setText(R.string.button_add_contact_state_added_text);
            buttonAddContact.setEnabled(false);
            buttonAddContact.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorNavy), PorterDuff.Mode.SRC);
        } else {
            buttonAddContact.setText(R.string.button_add_contact_state_unadded_text);
            buttonAddContact.setEnabled(true);
            buttonAddContact.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryLight), PorterDuff.Mode.SRC);
        }

        textViewContactName.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)));
        textViewPhoneNumber.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

        String image = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
        boolean invalidURI = true;
        if (image != null) {
            try {
                imageViewContactPhoto.setImageURI(Uri.parse(image));
                invalidURI = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(invalidURI) imageViewContactPhoto.setImageResource(R.drawable.unknown_user_icon);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonAddContact:
                View parentRow = (View) view.getParent();
                TextView textViewPhoneNumber = parentRow.findViewById(R.id.textViewPhoneNumber);
                contactListener.onContactAdded(textViewPhoneNumber.getText().toString());
                notifyDataSetChanged();
                break;
        }
    }

}
