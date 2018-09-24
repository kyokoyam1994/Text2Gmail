package com.example.kosko.text2gmail;

import android.content.Context;
import android.util.Log;

import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.MailConnectException;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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

    /*static {
        Security.addProvider(new JSSEProvider());
    }*/

    public GMailSender (Context context) {
        this.context = context;
    }

    private SMTPTransport connectToSmtp(String host, int port, String userEmail, String oauthToken, boolean debug) throws Exception {
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

        transport.issueCommand("AUTH XOAUTH2 " + new String(response), 235);
        return transport;
    }

    private boolean refreshAccessToken() {
        HttpURLConnection connection = null;
        try {
            URL endpoint = new URL("https://www.googleapis.com/oauth2/v4/token");
            connection = (HttpURLConnection) endpoint.openConnection();
            connection.setRequestMethod("POST");

            String dataParams = "&client_id=" + BuildConfig.Client_Id +
                    "&client_secret=" + BuildConfig.Secret_Id +
                    "&refresh_token=" + DefaultSharedPreferenceManager.getUserRefreshToken(context) +
                    "&grant_type=refresh_token";

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.append(dataParams);
            writer.flush();
            writer.close();
            os.close();

            connection.connect();
            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String jsonString = "";
                String line;
                while ((line = bufferedReader.readLine()) != null) jsonString += line;

                JSONObject jsonObject = new JSONObject(jsonString);
                String accessToken = jsonObject.getString("access_token");
                if(accessToken != null){
                    DefaultSharedPreferenceManager.setUserAccessToken(context, accessToken);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public synchronized void sendMail(String subject, String body, String user, String recipients) throws Exception {
        SMTPTransport smtpTransport = null;
        try {
            smtpTransport = connectToSmtp("smtp.gmail.com", 587, user, DefaultSharedPreferenceManager.getUserAccessToken(context), true);
        } catch (MailConnectException ex) {
            //ConnectException may occur here, possibly due to firewall issues
            Log.d(TAG, "Could not connect to SMTP, perhaps the firewall is blocking the connection...");
        } catch (MessagingException ex) {
            //Refresh access token and try again
            Log.d(TAG, "Initial SMTP connection failed, refreshing accessing token...");
            refreshAccessToken();
            smtpTransport = connectToSmtp("smtp.gmail.com", 587, user, DefaultSharedPreferenceManager.getUserAccessToken(context), true);
        }

        MimeMessage message = new MimeMessage(session);
        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
        message.setSender(new InternetAddress(user));
        message.setSubject(subject);
        message.setDataHandler(handler);

        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
        smtpTransport.sendMessage(message, message.getAllRecipients());
    }

}
