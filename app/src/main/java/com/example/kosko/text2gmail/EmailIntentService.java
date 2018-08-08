package com.example.kosko.text2gmail;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.example.kosko.text2gmail.database.AppDatabase;
import com.example.kosko.text2gmail.database.entity.LogEntry;
import com.example.kosko.text2gmail.fragment.MessageLogFragment;

import java.util.Date;

public class EmailIntentService extends IntentService {

    private final static String EMAIL_ADDRESS = "koskosyokoyama@gmail.com";
    private final static String PASSWORD = "password1234";

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
            String senderNumber = intent.getStringExtra(SMS_SENDER_NUMBER);
            String smsContents = intent.getStringExtra(SMS_CONTENTS);
            long smsDateReceived = intent.getLongExtra(SMS_DATE_RECEIVED, System.currentTimeMillis());

            String senderName = Util.findContactNameByNumber(this, senderNumber);
            String emailSubject = null;
            String emailBody = null;
            switch (intent.getStringExtra(EMAIL_TYPE)) {
                case EMAIL_TYPE_SMS:
                    emailSubject = "New Message From " + (senderName == null ? senderNumber : senderName);
                    emailBody = smsContents;
                    break;
                case EMAIL_TYPE_MISSED_CALL:
                    emailSubject = "Missed Call From " + (senderName == null ? senderNumber : senderName);
                    emailBody = "Call received on " + new Date(smsDateReceived).toString();
                    break;
                default:
                    return;
            }

            boolean sendSuccess = true;
            try {
                GmailSender sender = new GmailSender(EMAIL_ADDRESS, PASSWORD);
                sender.sendMail(emailSubject, emailBody, EMAIL_ADDRESS, EMAIL_ADDRESS);
            } catch (Exception e) {
                e.printStackTrace();
                sendSuccess = false;
            }

            LogEntry entry = new LogEntry(senderNumber, smsContents, new Date(smsDateReceived), sendSuccess);
            AppDatabase.getInstance(getApplicationContext()).logEntryDao().insert(entry);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MessageLogFragment.REFRESH_INTENT));
        }
    }

}
