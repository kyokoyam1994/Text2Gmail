package com.example.kosko.text2gmail.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.kosko.text2gmail.fragment.EmailConfigFragment;
import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;

import java.util.Calendar;

public class SchedulingModeBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = SchedulingModeBroadcastReceiver.class.getName();
    private static final int ALARM_CODE = 101;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm called");
        Toast.makeText(context,"Alarm started",  Toast.LENGTH_SHORT).show();
        startAlarm(context);
    }

    public static void startAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SchedulingModeBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        SchedulingModeQueryResult queryResult = querySchedule(context);
        Log.d(TAG, "Currently time: " + System.currentTimeMillis());
        Log.d(TAG, "Currently scheduled?: " + queryResult.isCurrScheduled() + ", Next schedule time: " + String.valueOf(queryResult.getNextScheduledTime()));
        DefaultSharedPreferenceManager.setCurrentlyScheduled(context, queryResult.isCurrScheduled());
        alarmManager.set(AlarmManager.RTC_WAKEUP, queryResult.getNextScheduledTime(), pendingIntent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(EmailConfigFragment.SCHEDULE_STATUS_INTENT));
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, SchedulingModeBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }

    public static SchedulingModeQueryResult querySchedule(Context context){
        Calendar curr = Calendar.getInstance();
        int dayOfWeek = curr.get(Calendar.DAY_OF_WEEK);
        int keyPos = dayOfWeek == 1 ? 6 : dayOfWeek - 2;
        String schedule = DefaultSharedPreferenceManager.getSchedule(context, DefaultSharedPreferenceManager.DAY_OF_THE_WEEK_KEYS[keyPos]);
        String[] intervals = schedule.split("~");

        Calendar c = (Calendar) curr.clone();
        Calendar c2 = (Calendar) curr.clone();
        String[] hm = intervals[0].split(":");
        String[] hm2 = intervals[1].split(":");

        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hm[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(hm[1]));
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        c2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hm2[0]));
        c2.set(Calendar.MINUTE, Integer.parseInt(hm2[1]));
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.MILLISECOND, 0);

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
