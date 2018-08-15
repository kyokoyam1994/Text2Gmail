package com.example.kosko.text2gmail.util;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.example.kosko.text2gmail.receiver.SMSMissedCallBroadcastReceiver;

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

    public static boolean isSMSMissedCallBroadcastReceiverOn(Context context){
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, SMSMissedCallBroadcastReceiver.class);
        int state = packageManager.getComponentEnabledSetting(componentName);
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

}
