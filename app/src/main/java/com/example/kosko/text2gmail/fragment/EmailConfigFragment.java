package com.example.kosko.text2gmail.fragment;

import android.Manifest;
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
import android.support.annotation.NonNull;
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
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.kosko.text2gmail.BuildConfig;
import com.example.kosko.text2gmail.DailySchedulerActivity;
import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.ScheduleEntry;
import com.example.kosko.text2gmail.database.AppDatabase;
import com.example.kosko.text2gmail.database.entity.RefreshToken;
import com.example.kosko.text2gmail.receiver.SchedulingModeBroadcastReceiver;
import com.example.kosko.text2gmail.receiver.SMSMissedCallBroadcastReceiver;
import com.example.kosko.text2gmail.retrofit.GoogleApiClient;
import com.example.kosko.text2gmail.retrofit.GoogleResponse;
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

import java.util.Calendar;
import java.util.List;

import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class EmailConfigFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = EmailConfigFragment.class.getName();

    public static final String SCHEDULE_STATUS_INTENT = "SCHEDULE_STATUS_INTENT";

    private static final int ACCOUNT_CODE = 101;
    private static final int RC_SERVICE_STATUS_PERMISSION_GRANTED = 202;

    private ScheduleStatusBroadcastReceiver scheduleStatusBroadcastReceiver;
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.email_config_fragment, container, false);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Constants.MAIL_GOOGLE_COM))
                .requestServerAuthCode(BuildConfig.Client_Id)
                .requestEmail()
                .build();
        gsClient = GoogleSignIn.getClient(getActivity(), gso);

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
        if (Util.isAccountConfigured(getActivity()) && Util.checkPermission(getActivity(), Constants.PERMISSIONS_CONTACTS)) {
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
                new AuthTokenTask(this, data).execute();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RC_SERVICE_STATUS_PERMISSION_GRANTED:
                Switch switchServiceStatus = getView().findViewById(R.id.switchServiceStatus);
                boolean permissionGranted = false;

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) permissionGranted = true;
                else Toast.makeText(getActivity(), "Needs permission for SMS", Toast.LENGTH_SHORT).show();

                PackageManager packageManager = getActivity().getPackageManager();
                ComponentName componentName = new ComponentName(getActivity(), SMSMissedCallBroadcastReceiver.class);
                int state = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                if (permissionGranted) state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                packageManager.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
                updateStatusCircle(getView(), permissionGranted);

                //Disable temporarily to prevent callback
                switchServiceStatus.setOnCheckedChangeListener(null);
                switchServiceStatus.setChecked(permissionGranted);
                switchServiceStatus.setOnCheckedChangeListener(this);
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

    private void configureSchedule() {
        Intent intent = new Intent(getActivity(), DailySchedulerActivity.class);
        startActivity(intent);
    }

    private void promptEmail() {
        Task<Void> task = gsClient.signOut();
        task.addOnSuccessListener(getActivity(), aVoid -> {
            Intent intent = gsClient.getSignInIntent();
            startActivityForResult(intent, ACCOUNT_CODE);
        });
    }

    private void updateConfiguredEmail(View view) {
        if (Util.isAccountConfigured(getActivity())) {
            TextView labelConfiguredEmailAddress = view.findViewById(R.id.labelConfiguredEmailAddress);
            ViewSwitcher viewSwitcher = view.findViewById(R.id.viewSwitcher);
            labelConfiguredEmailAddress.setText(DefaultSharedPreferenceManager.getUserEmail(getActivity()));
            if (viewSwitcher.getCurrentView() == view.findViewById(R.id.labelEmailUnconfigured)) {
                viewSwitcher.showNext();
            }
        }
    }

    private void deleteConfiguredEmail(View view) {
        DefaultSharedPreferenceManager.setUserEmail(getActivity(), null);
        DefaultSharedPreferenceManager.setUserAccessToken(getActivity(), null);
        DefaultSharedPreferenceManager.setUserRefreshToken(getActivity(), null);

        updateStatusCircle(view, Util.isSMSMissedCallBroadcastReceiverOn(getActivity()));

        ViewSwitcher viewSwitcher = view.findViewById(R.id.viewSwitcher);
        if(viewSwitcher.getCurrentView() == view.findViewById(R.id.cardViewConfiguredEmailAddress)){
            viewSwitcher.showNext();
        }
    }

    private void toggleServiceStatus(boolean isChecked) {
        if (Util.checkPermission(getActivity(), Constants.PERMISSIONS_SMS)) {
            PackageManager packageManager = getActivity().getPackageManager();
            ComponentName componentName = new ComponentName(getActivity(), SMSMissedCallBroadcastReceiver.class);
            int state = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            if (isChecked) state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            packageManager.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
            updateStatusCircle(getView(), isChecked);
        } else {
            requestPermissions(new String[] {Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, RC_SERVICE_STATUS_PERMISSION_GRANTED);
        }
    }

    private void toggleSchedulingMode(boolean isChecked) {
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

    private GoogleSignInClient getGoogleSignInClient() {
        return gsClient;
    }

    private static class AuthTokenTask extends AsyncTask<Void, Void, Boolean> {
        private EmailConfigFragment fragment;
        private Context context;
        private Intent data;

        public AuthTokenTask(EmailConfigFragment fragment, Intent data) {
            this.fragment = fragment;
            this.data = data;
            context = fragment.getContext().getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = false;
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String serverAuthCode = account.getServerAuthCode();
                Log.d(TAG, serverAuthCode);

                Response<GoogleResponse> response = GoogleApiClient.getInstance()
                    .getAccessToken(serverAuthCode, BuildConfig.Client_Id, BuildConfig.Secret_Id, "authorization_code", null).execute();

                if (response.isSuccessful()) {
                    GoogleResponse googleResponse = response.body();
                    String accessToken = googleResponse.getAccessToken();
                    String refreshToken = googleResponse.getRefreshToken();
                    if (refreshToken == null) {
                        List<RefreshToken> token = AppDatabase.getInstance(context).refreshTokenDao().getRefreshTokenByEmail(account.getEmail());
                        if (!token.isEmpty()) refreshToken = token.get(0).getRefreshToken();
                    }

                    if(account.getEmail() != null && accessToken != null && refreshToken != null) {
                        DefaultSharedPreferenceManager.setUserEmail(context, account.getEmail());
                        DefaultSharedPreferenceManager.setUserAccessToken(context, accessToken);
                        DefaultSharedPreferenceManager.setUserRefreshToken(context, refreshToken);
                        AppDatabase.getInstance(context).refreshTokenDao().insert(new RefreshToken(account.getEmail(), refreshToken));
                        Log.d(TAG, "SUCCESS!!");
                        success = true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (fragment != null && fragment.getActivity() != null && !fragment.getActivity().isFinishing() && !fragment.getActivity().isDestroyed()) {
                fragment.getGoogleSignInClient().signOut();
                View view = fragment.getView();
                if (success && view != null) {
                    fragment.updateConfiguredEmail(view);
                    fragment.updateStatusCircle(view, Util.isSMSMissedCallBroadcastReceiverOn(fragment.getActivity()));
                } else if (!success) {
                    Toast.makeText(fragment.getActivity(), "Could not sign in", Toast.LENGTH_SHORT).show();
                    //deleteConfiguredEmail(getView());
                }
            }
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