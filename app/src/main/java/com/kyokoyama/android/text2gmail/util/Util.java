package com.kyokoyama.android.text2gmail.util;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;

import com.kyokoyama.android.text2gmail.database.entity.LogEntry;
import com.kyokoyama.android.text2gmail.receiver.SMSMissedCallBroadcastReceiver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class Util {

    public static String findContactNameByNumber(Context context, String phoneNumber) {
        String contactName = null;
        if (checkPermission(context, Constants.PERMISSIONS_CONTACTS)) {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                cursor.close();
            }
        }
        return contactName;
    }

    public static boolean checkPermission(Context context, int permissionType) {
        switch (permissionType) {
            case Constants.PERMISSIONS_PHONE:
                return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
            case Constants.PERMISSIONS_SMS:
                return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
            case Constants.PERMISSIONS_CONTACTS:
                return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public static boolean isSMSMissedCallBroadcastReceiverOn(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, SMSMissedCallBroadcastReceiver.class);
        int state = packageManager.getComponentEnabledSetting(componentName);
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED && Util.checkPermission(context, Constants.PERMISSIONS_SMS);
    }

    public static boolean isAccountConfigured(Context context) {
        return DefaultSharedPreferenceManager.getUserEmail(context) != null
                && DefaultSharedPreferenceManager.getUserAccessToken(context) != null
                && DefaultSharedPreferenceManager.getUserRefreshToken(context) != null;
    }

    public static List<LogEntry> sortLogEntriesByContactName(Context context, List<LogEntry> logEntries) {
        TreeMap<String, ArrayList<LogEntry>> contactEntryMap = new TreeMap<>();
        for (LogEntry entry : logEntries) {
            String temp = Util.findContactNameByNumber(context, entry.getSenderNumber());
            String contactName = (temp == null ? entry.getSenderNumber() : temp);

            ArrayList<LogEntry> entryList;
            if(contactEntryMap.containsKey(contactName)) entryList = contactEntryMap.get(contactName);
            else entryList = new ArrayList<>();
            entryList.add(entry);
            contactEntryMap.put(contactName, entryList);
        }

        ArrayList<LogEntry> newLogEntries = new ArrayList<>();
        Iterator iterator = contactEntryMap.keySet().iterator();
        while (iterator.hasNext()) {
            newLogEntries.addAll(contactEntryMap.get(iterator.next()));
        }
        return newLogEntries;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

}
