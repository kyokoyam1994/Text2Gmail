package com.example.kosko.text2gmail;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;
import com.example.kosko.text2gmail.util.Util;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

public class GMailSender extends Authenticator {

    private static final String TAG = GMailSender.class.getName();
    private Context context;
    private Session session;

    public GMailSender (Context context) {
        this.context = context;
    }

    public SMTPTransport connectToSmtp(String host, int port, String userEmail, String oauthToken, boolean debug) throws Exception {
        Log.v(TAG, "came to connecttosmtp");

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.sasl.enable", "false");
        //props.put("mail.imaps.sasl.mechanisms.oauth2.oauthToken", oauthToken);
        session = Session.getInstance(props);
        session.setDebug(debug);

        final URLName unusedUrlName = null;
        SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
        // If the password is non-null, SMTP tries to do AUTH LOGIN.
        final String emptyPassword = null;
        transport.connect(host, port, userEmail, emptyPassword);
        Log.v(TAG, "came before gen response");
        byte[] response = String.format("user=%s\1auth=Bearer %s\1\1", userEmail, oauthToken).getBytes();
        response = BASE64EncoderStream.encode(response);

        Log.v(TAG, "came to call issuecommand " + transport.isConnected());
        Log.v(TAG , new String(response));

        try {
            transport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);
        } catch (MessagingException e) {
            //Try again after renewing authentication token
            Log.e(TAG, "Exception", e);
            Util.invalidateToken(context);
            AccountManagerFuture<Bundle> future = Util.requestToken(context, null);
            Bundle result = future.getResult();
            if (future.isDone() && !future.isCancelled() && result.getString(AccountManager.KEY_AUTHTOKEN) != null) {
                DefaultSharedPreferenceManager.setUserToken(context, result.getString(AccountManager.KEY_AUTHTOKEN));
                transport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);
                Log.d("TEST", "RENEWED AUTH TOKEN!");
            } else {
                throw new Exception();
            }
        }

        return transport;
    }

    public synchronized void sendMail(String subject, String body, String user, String oauthToken, String recipients) throws Exception {
        SMTPTransport smtpTransport = connectToSmtp("smtp.gmail.com", 587, user, oauthToken, true);

        MimeMessage message = new MimeMessage(session);
        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
        message.setSender(new InternetAddress(user));
        message.setSubject(subject);
        message.setDataHandler(handler);

        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
        smtpTransport.sendMessage(message, message.getAllRecipients());
    }

}
