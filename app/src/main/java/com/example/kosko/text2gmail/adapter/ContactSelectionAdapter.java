package com.example.kosko.text2gmail.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.kosko.text2gmail.R;

public class ContactSelectionAdapter extends CursorAdapter {

    public ContactSelectionAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.contact_list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textViewContactName = view.findViewById(R.id.textViewContactName);
        TextView textViewPhoneNumber = view.findViewById(R.id.textViewPhoneNumber);
        textViewContactName.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)));
        textViewPhoneNumber.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
    }

}
