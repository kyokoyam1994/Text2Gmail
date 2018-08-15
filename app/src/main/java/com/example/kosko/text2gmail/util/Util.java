package com.example.kosko.text2gmail.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class Util {

    public static String findContactNameByNumber(Context context, String phoneNumber){
        String contactName = null;
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }
        return contactName;
    }

}