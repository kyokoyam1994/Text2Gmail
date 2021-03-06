package com.kyokoyama.android.text2gmail.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.common.util.ArrayUtils;

public class DefaultSharedPreferenceManager {

    // Properties
    private static final String USER_EMAIL_KEY = "USER_EMAIL_KEY";
    private static final String USER_ACCESS_TOKEN_KEY = "USER_ACCESS_TOKEN_KEY";
    private static final String USER_REFRESH_TOKEN_KEY = "USER_REFRESH_TOKEN_KEY";
    private static final String SCHEDULING_MODE_KEY = "SCHEDULING_MODE_KEY";
    private static final String FORWARD_MISSED_CALLS_KEY = "FORWARD_MISSED_CALLS_KEY";

    public static final String MONDAY_SCHEDULE_KEY = "MONDAY_SCHEDULE_KEY";
    public static final String TUESDAY_SCHEDULE_KEY = "TUESDAY_SCHEDULE_KEY";
    public static final String WEDNESDAY_SCHEDULE_KEY = "WEDNESDAY_SCHEDULE_KEY";
    public static final String THURSDAY_SCHEDULE_KEY = "THURSDAY_SCHEDULE_KEY";
    public static final String FRIDAY_SCHEDULE_KEY = "FRIDAY_SCHEDULE_KEY";
    public static final String SATURDAY_SCHEDULE_KEY = "SATURDAY_SCHEDULE_KEY";
    public static final String SUNDAY_SCHEDULE_KEY = "SUNDAY_SCHEDULE_KEY";

    public static final String[] DAY_OF_THE_WEEK_KEYS = {MONDAY_SCHEDULE_KEY, TUESDAY_SCHEDULE_KEY,
                                WEDNESDAY_SCHEDULE_KEY, THURSDAY_SCHEDULE_KEY, FRIDAY_SCHEDULE_KEY,
                                SATURDAY_SCHEDULE_KEY, SUNDAY_SCHEDULE_KEY};

    private static final String DEFAULT_START_TIME = "9:00AM";
    private static final String DEFAULT_END_TIME = "5:00PM";
    private static final String DEFAULT_INTERVAL = DEFAULT_START_TIME + "~" + DEFAULT_END_TIME;

    public static String getUserEmail (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(USER_EMAIL_KEY, null);
    }

    public static void setUserEmail (Context context, String newValue) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(USER_EMAIL_KEY, newValue);
        editor.commit();
    }

    public static String getUserAccessToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(USER_ACCESS_TOKEN_KEY, null);
    }

    public static void setUserAccessToken(Context context, String newValue) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(USER_ACCESS_TOKEN_KEY, newValue);
        editor.commit();
    }

    public static String getUserRefreshToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(USER_REFRESH_TOKEN_KEY, null);
    }

    public static void setUserRefreshToken(Context context, String newValue) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(USER_REFRESH_TOKEN_KEY, newValue);
        editor.commit();
    }

    public static boolean getForwardMissedCalls (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FORWARD_MISSED_CALLS_KEY, false);
    }

    public static void setForwardMissedCalls (Context context, boolean newValue) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(FORWARD_MISSED_CALLS_KEY, newValue);
        editor.commit();
    }

    public static boolean getSchedulingMode (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SCHEDULING_MODE_KEY, false);
    }

    public static void setSchedulingMode (Context context, boolean newValue) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(SCHEDULING_MODE_KEY, newValue);
        editor.commit();
    }

    public static String getSchedule (Context context, String dayOfWeek) {
        if (ArrayUtils.contains(DAY_OF_THE_WEEK_KEYS, dayOfWeek)) return PreferenceManager.getDefaultSharedPreferences(context).getString(dayOfWeek, DEFAULT_INTERVAL);
        return null;
    }

    public static void setSchedule(Context context, String dayOfWeek, String newValue) {
        if (ArrayUtils.contains(DAY_OF_THE_WEEK_KEYS, dayOfWeek)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString(dayOfWeek, newValue);
            editor.commit();
        }
    }

}