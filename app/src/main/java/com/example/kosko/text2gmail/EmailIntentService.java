package com.example.kosko.text2gmail;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.kosko.text2gmail.database.AppDatabase;
import com.example.kosko.text2gmail.database.entity.BlockedContact;
import com.example.kosko.text2gmail.database.entity.LogEntry;
import com.example.kosko.text2gmail.fragment.MessageLogFragment;
import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;
import com.example.kosko.text2gmail.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmailIntentService extends IntentService {

    private static final String TAG = EmailIntentService.class.getName();

    public static final String EMAIL_TYPE = "EMAIL_TYPE";
    public static final String EMAIL_TYPE_SMS = "SMS";
    public static final String EMAIL_TYPE_MISSED_CALL = "MISSED_CALL";

    public static final String SMS_SENDER_NUMBER = "SMS_SENDER_NUMBER";
    public static final String SMS_CONTENTS = "SMS_CONTENTS";
    public static final String SMS_DATE_RECEIVED = "SMS_DATE_RECEIVED";

    public EmailIntentService() {
        this("EmailIntentService");
    }

    public EmailIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null) {
            List<BlockedContact> blockedContacts =  AppDatabase.getInstance(this).blockedContactDao().getAll();
            List<String> blockedNumbers = new ArrayList<>();
            for (BlockedContact contact : blockedContacts) blockedNumbers.add(contact.getBlockedNumber());

            String senderNumber = intent.getStringExtra(SMS_SENDER_NUMBER);
            String smsContents = intent.getStringExtra(SMS_CONTENTS);
            long smsDateReceived = intent.getLongExtra(SMS_DATE_RECEIVED, System.currentTimeMillis());


            Log.d(TAG, senderNumber);
            Log.d(TAG, blockedNumbers.toArray().toString());

            if (blockedNumbers.contains(senderNumber)){
                Log.d(TAG, senderNumber + "is blocked, ignoring...");
                return;
            }

            String senderName = Util.findContactNameByNumber(this, senderNumber);
            String emailSubject;
            String emailBody;
            switch (intent.getStringExtra(EMAIL_TYPE)) {
                case EMAIL_TYPE_SMS:
                    emailSubject = "New SMS Received From " + (senderName == null ? senderNumber : senderName);
                    emailBody = smsContents;
                    break;
                case EMAIL_TYPE_MISSED_CALL:
                    emailSubject = "Missed Call From " + (senderName == null ? senderNumber : senderName);
                    emailBody = "Call received on " + new Date(smsDateReceived).toString();
                    smsContents = emailSubject;
                    break;
                default:
                    return;
            }

            boolean sendSuccess = true;
            try {
                GMailSender sender = new GMailSender();
                sender.sendMail(emailSubject, emailBody, DefaultSharedPreferenceManager.getUserEmail(this),
                        DefaultSharedPreferenceManager.getUserToken(this), DefaultSharedPreferenceManager.getUserEmail(this));
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
                sendSuccess = false;
            }

            LogEntry entry = new LogEntry(senderNumber, smsContents, new Date(smsDateReceived), sendSuccess);
            AppDatabase.getInstance(getApplicationContext()).logEntryDao().insert(entry);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MessageLogFragment.REFRESH_INTENT));
        }
    }

}
