package com.example.kosko.text2gmail.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.example.kosko.text2gmail.BuildConfig;
import com.example.kosko.text2gmail.DailySchedulerActivity;
import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.ScheduleEntry;
import com.example.kosko.text2gmail.receiver.SchedulingModeBroadcastReceiver;
import com.example.kosko.text2gmail.receiver.SMSMissedCallBroadcastReceiver;
import com.example.kosko.text2gmail.util.Constants;
import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;
import com.example.kosko.text2gmail.util.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class EmailConfigFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = EmailConfigFragment.class.getName();
    public static final String SCHEDULE_STATUS_INTENT = "SCHEDULE_STATUS_INTENT";
    private static final int ACCOUNT_CODE = 101;

    private ScheduleStatusBroadcastReceiver scheduleStatusBroadcastReceiver;
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsoClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.email_config_fragment, container, false);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Constants.MAIL_GOOGLE_COM))
                .requestServerAuthCode(BuildConfig.Client_Id)
                .requestEmail()
                .build();
        gsoClient = GoogleSignIn.getClient(getActivity(), gso);

        Button setScheduleButton = view.findViewById(R.id.setScheduleButton);
        Switch switchServiceStatus = view.findViewById(R.id.switchServiceStatus);
        Switch switchSchedulingMode = view.findViewById(R.id.switchSchedulingMode);
        Button configureEmailButton = view.findViewById(R.id.configureEmailButton);
        ImageButton buttonDeleteConfiguredEmailAddress = view.findViewById(R.id.buttonDeleteConfiguredEmailAddress);

        switchServiceStatus.setChecked(Util.isSMSMissedCallBroadcastReceiverOn(getActivity()));
        switchSchedulingMode.setChecked(DefaultSharedPreferenceManager.getSchedulingMode(getActivity()));

        setScheduleButton.setOnClickListener(this);
        switchServiceStatus.setOnCheckedChangeListener(this);
        switchSchedulingMode.setOnCheckedChangeListener(this);
        configureEmailButton.setOnClickListener(this);
        buttonDeleteConfiguredEmailAddress.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        //Perform check here to see whether Google account still exists on device, remove it from configured email if not
        super.onResume();
        if (Util.isAccountConfigured(getActivity())) {
            String user = DefaultSharedPreferenceManager.getUserEmail(getActivity());
            Account userAccount = null;
            AccountManager accountManager = AccountManager.get(getActivity());
            for (Account account : accountManager.getAccountsByType("com.google")) {
                if (account.name.equals(user)) {
                    userAccount = account;
                    break;
                }
            }

            if(userAccount == null) {
                Log.d(TAG, "Account no longer exists, removing...");
                deleteConfiguredEmail(getView());
            }
        }

        updateConfiguredEmail(getView());
        updateStatusCircle(getView(), Util.isSMSMissedCallBroadcastReceiverOn(getActivity()));

        scheduleStatusBroadcastReceiver = new ScheduleStatusBroadcastReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(scheduleStatusBroadcastReceiver, new IntentFilter(SCHEDULE_STATUS_INTENT));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(scheduleStatusBroadcastReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, resultCode + ", " + requestCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == ACCOUNT_CODE) {
                new AuthTokenTask(data).execute();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setScheduleButton:
                configureSchedule();
                break;
            case R.id.configureEmailButton:
                promptEmail();
                break;
            case R.id.buttonDeleteConfiguredEmailAddress:
                deleteConfiguredEmail(getView());
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switchServiceStatus:
                toggleServiceStatus(isChecked);
                break;
            case R.id.switchSchedulingMode:
                toggleSchedulingMode(isChecked);
                break;
        }
    }

    public void configureSchedule() {
        Intent intent = new Intent(getActivity(), DailySchedulerActivity.class);
        startActivity(intent);
    }

    public void promptEmail() {
        Task<Void> task = gsoClient.signOut();
        task.addOnSuccessListener(getActivity(), aVoid -> {
            Intent intent = gsoClient.getSignInIntent();
            startActivityForResult(intent, ACCOUNT_CODE);
        });
    }

    public void updateConfiguredEmail(View view) {
        if (Util.isAccountConfigured(getActivity())) {
            TextView labelConfiguredEmailAddress = view.findViewById(R.id.labelConfiguredEmailAddress);
            ViewSwitcher viewSwitcher = view.findViewById(R.id.viewSwitcher);
            labelConfiguredEmailAddress.setText(DefaultSharedPreferenceManager.getUserEmail(getActivity()));
            if (viewSwitcher.getCurrentView() == view.findViewById(R.id.labelEmailUnconfigured)) {
                viewSwitcher.showNext();
            }
        }
    }

    public void deleteConfiguredEmail(View view) {
        DefaultSharedPreferenceManager.setUserEmail(getActivity(), null);
        DefaultSharedPreferenceManager.setUserAccessToken(getActivity(), null);
        DefaultSharedPreferenceManager.setUserRefreshToken(getActivity(), null);

        updateStatusCircle(view, Util.isSMSMissedCallBroadcastReceiverOn(getActivity()));

        ViewSwitcher viewSwitcher = view.findViewById(R.id.viewSwitcher);
        if(viewSwitcher.getCurrentView() == view.findViewById(R.id.cardViewConfiguredEmailAddress)){
            viewSwitcher.showNext();
        }
    }

    public void toggleServiceStatus(boolean isChecked) {
        PackageManager packageManager = getActivity().getPackageManager();
        ComponentName componentName = new ComponentName(getActivity(), SMSMissedCallBroadcastReceiver.class);
        int state = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        if (isChecked) state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        packageManager.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
        updateStatusCircle(getView(), isChecked);
    }

    public void toggleSchedulingMode(boolean isChecked) {
        DefaultSharedPreferenceManager.setSchedulingMode(getActivity(), isChecked);
        Switch switchServiceStatus = getView().findViewById(R.id.switchServiceStatus);

        if(isChecked) SchedulingModeBroadcastReceiver.startAlarm(getActivity());
        else SchedulingModeBroadcastReceiver.cancelAlarm(getActivity());
        updateStatusCircle(getView(), switchServiceStatus.isChecked());
    }

    private void updateStatusCircle(View view, boolean serviceOn) {
        ImageView statusCircle = view.findViewById(R.id.statusCircle);
        TextView labelStatus = view.findViewById(R.id.labelStatus);
        TextView labelScheduleTime = view.findViewById(R.id.labelScheduleTime);
        boolean schedulingModeOn = DefaultSharedPreferenceManager.getSchedulingMode(getActivity());
        SchedulingModeBroadcastReceiver.SchedulingModeQueryResult queryResult = SchedulingModeBroadcastReceiver.querySchedule(getActivity());
        boolean currentlyScheduled = queryResult.isCurrScheduled();

        if (!Util.isAccountConfigured(getActivity())) {
            statusCircle.getDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGray), PorterDuff.Mode.SRC);
            labelStatus.setText(R.string.status_label_text_not_configured);
        } else if (serviceOn) {
            if (schedulingModeOn && !currentlyScheduled){
                statusCircle.getDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorYellow), PorterDuff.Mode.SRC);
                labelStatus.setText(R.string.status_label_text_not_scheduled);
            } else {
                statusCircle.getDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGreen), PorterDuff.Mode.SRC);
                labelStatus.setText(R.string.status_label_text_running);
            }
        } else {
            statusCircle.getDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorRed), PorterDuff.Mode.SRC);
            labelStatus.setText(R.string.status_label_text_stopped);
        }

        if (schedulingModeOn){
            Calendar curr = Calendar.getInstance();
            int dayOfWeek = curr.get(Calendar.DAY_OF_WEEK);
            ScheduleEntry.DayOfTheWeek dayOfTheWeekEnum = ScheduleEntry.DayOfTheWeek.from(dayOfWeek == 1 ? 7 : dayOfWeek - 1);

            String scheduledTime = DefaultSharedPreferenceManager.getSchedule(getActivity(), DefaultSharedPreferenceManager.DAY_OF_THE_WEEK_KEYS[dayOfWeek == 1 ? 6 : dayOfWeek - 2]);
            labelScheduleTime.setText(dayOfTheWeekEnum.getName() + ", " + scheduledTime);
        } else labelScheduleTime.setText(getResources().getString(R.string.label_schedule_time_off_text));
    }


    private class AuthTokenTask extends AsyncTask<Void, Void, Boolean> {
        private Intent data;
        public AuthTokenTask(Intent data) {
            this.data = data;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = false;
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            HttpURLConnection connection = null;
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String serverAuthCode = account.getServerAuthCode();
                Log.d(TAG, serverAuthCode);
                URL endpoint = new URL("https://www.googleapis.com/oauth2/v4/token");
                connection = (HttpURLConnection) endpoint.openConnection();
                connection.setRequestMethod("POST");

                String dataParams = "code=" + serverAuthCode +
                        "&client_id=" + BuildConfig.Client_Id +
                        "&client_secret=" + BuildConfig.Secret_Id +
                        "&grant_type=authorization_code";
                Log.d(TAG, dataParams);

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
                    Log.d(TAG, jsonString);

                    String accessToken = jsonObject.getString("access_token");
                    String refreshToken = jsonObject.getString("refresh_token");
                    if(account.getEmail() != null && accessToken != null && refreshToken != null) {
                        DefaultSharedPreferenceManager.setUserEmail(getActivity(), account.getEmail());
                        DefaultSharedPreferenceManager.setUserAccessToken(getActivity(), accessToken);
                        DefaultSharedPreferenceManager.setUserRefreshToken(getActivity(), refreshToken);
                        Log.d(TAG, "SUCCESS!!");
                        success = true;
                    }
                }else Log.d("TAG", String.valueOf(responseCode));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
                Log.d(TAG, "REVOKING");
                //gsoClient.signOut();
                gsoClient.revokeAccess().addOnCompleteListener(revokeTask -> gsoClient.signOut());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                updateConfiguredEmail(getView());
                updateStatusCircle(getView(), Util.isSMSMissedCallBroadcastReceiverOn(getActivity()));
            } else deleteConfiguredEmail(getView());
        }
    }

    private class ScheduleStatusBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Switch switchServiceStatus = getView().findViewById(R.id.switchServiceStatus);
            updateStatusCircle(getView(), switchServiceStatus.isChecked());
        }
    }

}