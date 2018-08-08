package com.example.kosko.text2gmail.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.example.kosko.text2gmail.EmailIntentService;
import com.example.kosko.text2gmail.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class SMSMissedCallBroadcastReceiver extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean forwardMissedCalls = preferences.getBoolean(context.getString(R.string.forward_missed_calls_key), true);

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && extras != null && extras.containsKey("pdus")) {
            Toast.makeText(context,"Sending text...",  Toast.LENGTH_SHORT).show();
            Object[] pdus = (Object[]) extras.get("pdus");
            HashMap<String, ArrayList<SmsMessage>> messageMap = new HashMap<>();
            for (Object pdu : pdus) {
                SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu, extras.getString("format"));
                ArrayList<SmsMessage> messageList = new ArrayList<>();
                if (messageMap.containsKey(message.getOriginatingAddress())) {
                    messageList = messageMap.get(message.getOriginatingAddress());
                }
                messageList.add(message);
                messageMap.put(message.getOriginatingAddress(), messageList);
            }

            Iterator<String> iterator = messageMap.keySet().iterator();
            while (iterator.hasNext()){
                String address = iterator.next();
                ArrayList<SmsMessage> messageList = messageMap.get(address);
                String message = concatSMSMessages(messageList);
                long timestamp = messageList.get(0).getTimestampMillis();

                Intent emailIntent = new Intent(context, EmailIntentService.class);
                emailIntent.putExtra(EmailIntentService.EMAIL_TYPE, EmailIntentService.EMAIL_TYPE_SMS);
                emailIntent.putExtra(EmailIntentService.SMS_SENDER_NUMBER, address);
                emailIntent.putExtra(EmailIntentService.SMS_CONTENTS, message);
                emailIntent.putExtra(EmailIntentService.SMS_DATE_RECEIVED, timestamp);
                context.startService(emailIntent);
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

    //Method operates on the assumption that all messages in the list come from the same sender
    private String concatSMSMessages(ArrayList<SmsMessage> messageList){
        String result = "";
        for (SmsMessage message : messageList) {
            result += message.getMessageBody();
        }
        return result;
    }

    protected void onIncomingCallStarted(Context ctx, String number, Date start){}

    protected void onOutgoingCallStarted(Context ctx, String number, Date start){}

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end){}

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){}

    protected void onMissedCall(Context ctx, String number, Date start){
        Intent emailIntent = new Intent(ctx, EmailIntentService.class);
        emailIntent.putExtra(EmailIntentService.EMAIL_TYPE, EmailIntentService.EMAIL_TYPE_MISSED_CALL);
        emailIntent.putExtra(EmailIntentService.SMS_SENDER_NUMBER, number);
        emailIntent.putExtra(EmailIntentService.SMS_DATE_RECEIVED, start.getTime());
        ctx.startService(emailIntent);
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
}