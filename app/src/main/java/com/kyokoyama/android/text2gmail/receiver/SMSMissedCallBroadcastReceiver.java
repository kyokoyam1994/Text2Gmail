package com.kyokoyama.android.text2gmail.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.kyokoyama.android.text2gmail.EmailIntentService;
import com.kyokoyama.android.text2gmail.util.DefaultSharedPreferenceManager;
import com.kyokoyama.android.text2gmail.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class SMSMissedCallBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = SMSMissedCallBroadcastReceiver.class.getName();

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        boolean forwardMissedCalls = DefaultSharedPreferenceManager.getForwardMissedCalls(context);
        boolean schedulingMode = DefaultSharedPreferenceManager.getSchedulingMode(context);
        SchedulingModeBroadcastReceiver.SchedulingModeQueryResult queryResult = SchedulingModeBroadcastReceiver.querySchedule(context);
        boolean currentlyScheduled = queryResult.isCurrScheduled();

        if (!Util.isAccountConfigured(context)) {
            Log.d(TAG, "Email is not configured!");
        } else if (schedulingMode && !currentlyScheduled) {
            Log.d(TAG, "Not currently scheduled!");
        } else if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && extras != null && extras.containsKey("pdus")) {
            Toast.makeText(context,"Sending SMS Email...",  Toast.LENGTH_SHORT).show();
            Object[] pdus = (Object[]) extras.get("pdus");
            HashMap<String, ArrayList<SmsMessage>> messageMap = new HashMap<>();
            for (Object pdu : pdus) {
                SmsMessage message;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    message = SmsMessage.createFromPdu((byte[]) pdu, extras.getString("format"));
                }else {
                    message = SmsMessage.createFromPdu((byte[]) pdu);
                }

                ArrayList<SmsMessage> messageList = new ArrayList<>();
                if (messageMap.containsKey(message.getOriginatingAddress())) {
                    messageList = messageMap.get(message.getOriginatingAddress());
                }
                messageList.add(message);
                messageMap.put(message.getOriginatingAddress(), messageList);
            }

            Iterator<String> iterator = messageMap.keySet().iterator();
            while (iterator.hasNext()) {
                String address = iterator.next();
                ArrayList<SmsMessage> messageList = messageMap.get(address);
                String message = concatSMSMessages(messageList);
                long timestamp = messageList.get(0).getTimestampMillis();
                startEmailService(context, EmailIntentService.EMAIL_TYPE_SMS, address, message, timestamp);
            }
        } else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL") && forwardMissedCalls) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else if (intent.getAction().equals("android.intent.action.PHONE_STATE") && forwardMissedCalls) {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) state = TelephonyManager.CALL_STATE_IDLE;
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) state = TelephonyManager.CALL_STATE_OFFHOOK;
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) state = TelephonyManager.CALL_STATE_RINGING;

            onCallStateChanged(context, state, number);
        }
    }

    protected void onIncomingCallStarted(Context context, String number, Date start) {}

    protected void onOutgoingCallStarted(Context context, String number, Date start) {}

    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {}

    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {}

    protected void onMissedCall(Context context, String number, Date start) {
        startEmailService(context, EmailIntentService.EMAIL_TYPE_MISSED_CALL, number, null, start.getTime());
    }

    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state) return;
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if(lastState == TelephonyManager.CALL_STATE_RINGING) onMissedCall(context, savedNumber, callStartTime);
                else if(isIncoming) onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                else onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                break;
        }
        lastState = state;
    }

    //Method operates on the assumption that all messages in the list come from the same sender
    private String concatSMSMessages(ArrayList<SmsMessage> messageList) {
        StringBuilder builder = new StringBuilder();
        for (SmsMessage message : messageList) builder.append(message.getMessageBody());
        return builder.toString();
    }

    private void startEmailService(Context context, String emailType, String senderNumber, String contents, long dateReceived) {
        Intent emailIntent = new Intent(context, EmailIntentService.class);
        emailIntent.putExtra(EmailIntentService.EMAIL_TYPE, emailType);
        emailIntent.putExtra(EmailIntentService.SMS_SENDER_NUMBER, senderNumber);
        emailIntent.putExtra(EmailIntentService.SMS_CONTENTS, contents);
        emailIntent.putExtra(EmailIntentService.SMS_DATE_RECEIVED, dateReceived);
        context.startService(emailIntent);
    }

}