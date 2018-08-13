package com.example.kosko.text2gmail.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DefaultSharedPreferenceManager {

    // Properties
    private static final String USER_EMAIL_KEY = "USER_EMAIL_KEY";
    private static final String USER_TOKEN_KEY = "USER_TOKEN_KEY";
    private static final String FORWARD_MISSED_CALLS_KEY = "FORWARD_MISSED_CALLS_KEY";

    public static String getUserEmail (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(USER_EMAIL_KEY, null);
    }

    public static void setUserEmail (Context context, String newValue) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(USER_EMAIL_KEY, newValue);
        editor.commit();
    }

    public static String getUserToken (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(USER_TOKEN_KEY, null);
    }

    public static void setUserToken (Context context, String newValue) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(USER_TOKEN_KEY, newValue);
        editor.commit();
    }

    public static boolean getForwardMissedCalls (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FORWARD_MISSED_CALLS_KEY, true);
    }

    public static void setForwardMissedCalls (Context context, boolean newValue) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(FORWARD_MISSED_CALLS_KEY, newValue);
        editor.commit();
    }

}