package com.example.kosko.text2gmail.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.kosko.text2gmail.fragment.EmailConfigFragment;
import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;
import java.util.Date;

public class SchedulingModeBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = SchedulingModeBroadcastReceiver.class.getName();
    public static final int ALARM_CODE = 301;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Intent is.. " + intent.getAction());
        if (intent.getAction() != null) {
            if (intent.getAction().equals("com.example.kosko.text2gmail.receiver.SchedulingModeBroadcastReceiver") ||
                (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && DefaultSharedPreferenceManager.getSchedulingMode(context))) {
                    startAlarm(context);
            } else if ( DefaultSharedPreferenceManager.getSchedulingMode(context) &&
                (intent.getAction().equals(Intent.ACTION_DATE_CHANGED) ||
                intent.getAction().equals(Intent.ACTION_TIME_CHANGED) ||
                intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED))) {
                    cancelAlarm(context);
                    startAlarm(context);
            }
        }
    }

    public static void startAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("com.example.kosko.text2gmail.receiver.SchedulingModeBroadcastReceiver", null, context, SchedulingModeBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        SchedulingModeQueryResult queryResult = querySchedule(context);
        alarmManager.set(AlarmManager.RTC_WAKEUP, queryResult.getNextScheduledTime(), pendingIntent);
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, queryResult.getNextScheduledTime(), pendingIntent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(EmailConfigFragment.SCHEDULE_STATUS_INTENT));
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent("com.example.kosko.text2gmail.receiver.SchedulingModeBroadcastReceiver", null, context, SchedulingModeBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }

    public static boolean isAlarmActive(Context context) {
        return (PendingIntent.getBroadcast(context, ALARM_CODE,
            new Intent("com.example.kosko.text2gmail.receiver.SchedulingModeBroadcastReceiver", null, context, SchedulingModeBroadcastReceiver.class),
            PendingIntent.FLAG_NO_CREATE) != null);
    }

    public static SchedulingModeQueryResult querySchedule(Context context){
        Calendar curr = Calendar.getInstance();
        int dayOfWeek = curr.get(Calendar.DAY_OF_WEEK);
        int keyPos = dayOfWeek == 1 ? 6 : dayOfWeek - 2;
        String schedule = DefaultSharedPreferenceManager.getSchedule(context, DefaultSharedPreferenceManager.DAY_OF_THE_WEEK_KEYS[keyPos]);
        String[] intervals = schedule.split("~");

        /*DateTimeFormatter parseFormat = new DateTimeFormatterBuilder().appendPattern("h:mma").toFormatter();
        LocalTime localTime = LocalTime.parse(intervals[0], parseFormat);
        LocalTime localTime2 = LocalTime.parse(intervals[1], parseFormat);
        Calendar c = (Calendar) curr.clone();
        Calendar c2 = (Calendar) curr.clone();

        c.set(Calendar.HOUR_OF_DAY, localTime.getHour());
        c.set(Calendar.MINUTE, localTime.getMinute());
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        c2.set(Calendar.HOUR_OF_DAY, localTime2.getHour());
        c2.set(Calendar.MINUTE, localTime2.getMinute());
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.MILLISECOND, 0);*/

        Calendar c = (Calendar) curr.clone();
        Calendar c2 = (Calendar) curr.clone();
        DateFormat format = new SimpleDateFormat("h:mma");
        try {
            Date parsedTime = format.parse(intervals[0]);
            Date parsedTime2 = format.parse(intervals[1]);
            Calendar temp = Calendar.getInstance();
            Calendar temp2 = Calendar.getInstance();
            temp.setTime(parsedTime);
            temp2.setTime(parsedTime2);

            c.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
            c.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            c2.set(Calendar.HOUR_OF_DAY, temp2.get(Calendar.HOUR_OF_DAY));
            c2.set(Calendar.MINUTE, temp2.get(Calendar.MINUTE));
            c2.set(Calendar.SECOND, 0);
            c2.set(Calendar.MILLISECOND, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        boolean isCurrScheduled = false;
        long nextScheduledTime;

        if(curr.before(c)) nextScheduledTime = c.getTime().getTime();
        else if(curr.after(c2)){
            //Return start of next day
            curr.add(Calendar.DAY_OF_MONTH, 1);
            curr.set(Calendar.HOUR_OF_DAY, 0);
            curr.set(Calendar.MINUTE, 0);
            curr.set(Calendar.SECOND, 0);
            curr.set(Calendar.MILLISECOND, 0);
            nextScheduledTime = curr.getTime().getTime();
        }
        else{
            nextScheduledTime = c2.getTime().getTime();
            isCurrScheduled = true;
        }
        return new SchedulingModeQueryResult(isCurrScheduled, nextScheduledTime);
    }

    public static class SchedulingModeQueryResult {

        private boolean isCurrScheduled;
        private long nextScheduledTime;

        public SchedulingModeQueryResult(boolean isCurrScheduled, long nextScheduledTime) {
            this.isCurrScheduled = isCurrScheduled;
            this.nextScheduledTime = nextScheduledTime;
        }

        public boolean isCurrScheduled() {
            return isCurrScheduled;
        }

        public long getNextScheduledTime() {
            return nextScheduledTime;
        }
    }

}
