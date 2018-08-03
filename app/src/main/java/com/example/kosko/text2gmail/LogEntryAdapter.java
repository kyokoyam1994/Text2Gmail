package com.example.kosko.text2gmail;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kosko.text2gmail.database.entity.LogEntry;

import java.util.List;

public class LogEntryAdapter extends ArrayAdapter<LogEntry> {

    public LogEntryAdapter(@NonNull Context context, int resource, @NonNull List<LogEntry> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if(v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.log_entry, parent, false);
        }

        LogEntry entry = getItem(position);
        TextView sender = v.findViewById(R.id.logEntrySenderTextView);
        TextView message = v.findViewById(R.id.logEntryMessageTextView);
        TextView date = v.findViewById(R.id.logEntryDateTextView);
        TextView sendSuccessful = v.findViewById(R.id.logEntrySendSuccessful);

        //Find a way to move this method outside of the main thread when calling from getView()
        String contactName = findContactNameByNumber(entry.getSenderNumber());
        sender.setText(contactName == null ? entry.getSenderNumber() : contactName);
        message.setText(entry.getMessage());
        date.setText(entry.getDateReceived().toString());

        if(entry.isSendSuccessful()) sendSuccessful.setText("Sent");
        else sendSuccessful.setText("Failed");

        return v;
    }

    private String findContactNameByNumber(String phoneNumber){
        //Should check for permission here for contacts access
        String contactName = null;
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }
        return contactName;
    }

}
