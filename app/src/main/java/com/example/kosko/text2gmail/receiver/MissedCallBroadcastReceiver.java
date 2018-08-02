package com.example.kosko.text2gmail.receiver;

import android.content.Context;
import android.content.Intent;

import com.example.kosko.text2gmail.EmailIntentService;

import java.util.Date;

public class MissedCallBroadcastReceiver extends PhoneCallBroadcastReceiver {

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Intent emailIntent = new Intent(ctx, EmailIntentService.class);
        emailIntent.putExtra(EmailIntentService.EMAIL_TYPE, EmailIntentService.EMAIL_TYPE_MISSED_CALL);
        emailIntent.putExtra(EmailIntentService.SMS_SENDER_NUMBER, number);
        emailIntent.putExtra(EmailIntentService.SMS_SENDER_NAME, "Unknown");
        emailIntent.putExtra(EmailIntentService.SMS_DATE_RECEIVED, start.getTime());
        ctx.startService(emailIntent);
    }

}
