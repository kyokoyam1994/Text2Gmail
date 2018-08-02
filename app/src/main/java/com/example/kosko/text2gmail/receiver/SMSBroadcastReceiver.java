package com.example.kosko.text2gmail.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.example.kosko.text2gmail.EmailIntentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Test");
        Bundle extras = intent.getExtras();
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION) && extras != null && extras.containsKey("pdus")) {
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
                emailIntent.putExtra(EmailIntentService.SMS_SENDER_NAME, "Unknown");
                emailIntent.putExtra(EmailIntentService.SMS_CONTENTS, message);
                emailIntent.putExtra(EmailIntentService.SMS_DATE_RECEIVED, timestamp);
                context.startService(emailIntent);
            }
        }
    }

    //Assume that all messages in the list come from the same sender
    private String concatSMSMessages(ArrayList<SmsMessage> messageList){
        String result = "";
        for (SmsMessage message : messageList) {
            result += message.getMessageBody();
        }
        return result;
    }

}