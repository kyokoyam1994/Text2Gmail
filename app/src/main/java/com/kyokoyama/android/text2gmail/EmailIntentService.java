package com.kyokoyama.android.text2gmail;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.kyokoyama.android.text2gmail.database.AppDatabase;
import com.kyokoyama.android.text2gmail.database.entity.BlockedContact;
import com.kyokoyama.android.text2gmail.database.entity.LogEntry;
import com.kyokoyama.android.text2gmail.fragment.MessageLogFragment;
import com.kyokoyama.android.text2gmail.util.DefaultSharedPreferenceManager;
import com.kyokoyama.android.text2gmail.util.Util;

import java.util.Date;
import java.util.List;

public class EmailIntentService extends JobIntentService {

    private static final String TAG = EmailIntentService.class.getName();
    private static final int JOB_ID = 1000;

    public static final String EMAIL_TYPE = "EMAIL_TYPE";
    public static final String EMAIL_TYPE_SMS = "SMS";
    public static final String EMAIL_TYPE_MISSED_CALL = "MISSED_CALL";

    public static final String SMS_SENDER_NUMBER = "SMS_SENDER_NUMBER";
    public static final String SMS_CONTENTS = "SMS_CONTENTS";
    public static final String SMS_DATE_RECEIVED = "SMS_DATE_RECEIVED";

    public EmailIntentService() {
        super();
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, EmailIntentService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if(intent != null) {
            List<BlockedContact> blockedContacts =  AppDatabase.getInstance(this).blockedContactDao().getAll();

            String senderNumber = intent.getStringExtra(SMS_SENDER_NUMBER);
            String smsContents = intent.getStringExtra(SMS_CONTENTS);
            long smsDateReceived = intent.getLongExtra(SMS_DATE_RECEIVED, System.currentTimeMillis());

            for (BlockedContact contact : blockedContacts) {
                if (PhoneNumberUtils.compare(contact.getBlockedNumber(), senderNumber)){
                    Log.d(TAG, senderNumber + " is blocked, ignoring...");
                    return;
                }
            }

            String temp = Util.findContactNameByNumber(this, senderNumber);
            String senderName = (temp == null ? senderNumber : temp);
            String emailSubject;
            String emailBody;

            switch (intent.getStringExtra(EMAIL_TYPE)) {
                case EMAIL_TYPE_SMS:
                    emailSubject = "[Text2Gmail] You've got a new text from " + senderName + "!";
                    emailBody = constructSMSReceivedBody(senderName, smsContents, smsDateReceived);
                    break;
                case EMAIL_TYPE_MISSED_CALL:
                    emailSubject = "[Text2Gmail] You missed a call from " + senderName + "!";
                    emailBody = constructMissedCallBody(senderName, smsDateReceived);
                    smsContents = "Missed call from " + senderName;
                    break;
                default:
                    return;
            }

            boolean sendSuccess = true;
            try {
                Log.d(TAG, DefaultSharedPreferenceManager.getUserEmail(this));
                GMailSender sender = new GMailSender(this);
                sender.sendMail(emailSubject, emailBody, DefaultSharedPreferenceManager.getUserEmail(this), DefaultSharedPreferenceManager.getUserEmail(this));
            } catch (Exception e) {
                //Toast.makeText(this, "Could not send e-mail", Toast.LENGTH_SHORT);
                Log.e(TAG, "Exception", e);
                sendSuccess = false;
            }

            LogEntry entry = new LogEntry(senderNumber, smsContents, new Date(smsDateReceived), sendSuccess);
            AppDatabase.getInstance(getApplicationContext()).logEntryDao().insert(entry);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MessageLogFragment.REFRESH_INTENT));
        }
    }

    private String constructSMSReceivedBody(String sender, String message, long timestamp) {
        return new StringBuilder().append(sender)
                .append(" said...\n\n")
                .append(message)
                .append("\n\nSent at ")
                .append(new Date(timestamp).toString()).toString();
    }

    private String constructMissedCallBody(String sender, long timestamp) {
        return new StringBuilder().append(sender)
                .append(" called you at ")
                .append(new Date(timestamp).toString()).toString();
    }

}
