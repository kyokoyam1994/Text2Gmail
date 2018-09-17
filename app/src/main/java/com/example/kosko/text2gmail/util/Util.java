package com.example.kosko.text2gmail.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.example.kosko.text2gmail.database.entity.LogEntry;
import com.example.kosko.text2gmail.receiver.SMSMissedCallBroadcastReceiver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class Util {

    private static final String SCOPE = Constants.GMAIL_COMPOSE + " " + Constants.GMAIL_MODIFY + " " + Constants.MAIL_GOOGLE_COM;

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

    public static List<LogEntry> sortLogEntriesByContactName(Context context, List<LogEntry> logEntries){
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
        while (iterator.hasNext()){
            newLogEntries.addAll(contactEntryMap.get(iterator.next()));
        }
        return newLogEntries;
    }

    public static void invalidateToken(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        accountManager.invalidateAuthToken("com.google", DefaultSharedPreferenceManager.getUserToken(context));
        DefaultSharedPreferenceManager.setUserToken(context, null);
    }

    public static AccountManagerFuture<Bundle> requestToken(Context context, AccountManagerCallback<Bundle> callback) {
        Account userAccount = null;
        AccountManager accountManager = AccountManager.get(context);
        String user = DefaultSharedPreferenceManager.getUserEmail(context);
        for (Account account : accountManager.getAccountsByType("com.google")) {
            if (account.name.equals(user)) {
                userAccount = account;
                break;
            }
        }

        if (context instanceof Activity) return accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, (Activity) context, callback, null);
        else return accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, true, callback, null);
    }


}
