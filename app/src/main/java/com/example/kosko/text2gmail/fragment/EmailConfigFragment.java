package com.example.kosko.text2gmail.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
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

import com.example.kosko.text2gmail.DailySchedulerActivity;
import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.receiver.SchedulingModeBroadcastReceiver;
import com.example.kosko.text2gmail.receiver.SMSMissedCallBroadcastReceiver;
import com.example.kosko.text2gmail.util.Constants;
import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;
import com.example.kosko.text2gmail.util.Util;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class EmailConfigFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = EmailConfigFragment.class.getName();
    private final String SCOPE = Constants.GMAIL_COMPOSE + " " + Constants.GMAIL_MODIFY + " " + Constants.MAIL_GOOGLE_COM;

    private ScheduleStatusBroadcastReceiver scheduleStatusBroadcastReceiver;
    public final static String SCHEDULE_STATUS_INTENT = "SCHEDULE_STATUS_INTENT";

    private static final int AUTHORIZATION_CODE = 101;
    private static final int ACCOUNT_CODE = 201;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.email_config_fragment, container, false);

        Button setScheduleButton = view.findViewById(R.id.setScheduleButton);
        Switch switchServiceStatus = view.findViewById(R.id.switchServiceStatus);
        Switch switchSchedulingMode = view.findViewById(R.id.switchSchedulingMode);
        Button configureEmailButton = view.findViewById(R.id.configureEmailButton);
        ImageButton buttonDeleteConfiguredEmailAddress = view.findViewById(R.id.buttonDeleteConfiguredEmailAddress);

        //boolean isReceiverOn = Util.isSMSMissedCallBroadcastReceiverOn(getActivity());
        switchServiceStatus.setChecked(Util.isSMSMissedCallBroadcastReceiverOn(getActivity()));
        switchSchedulingMode.setChecked(DefaultSharedPreferenceManager.getSchedulingMode(getActivity()));
        //updateConfiguredEmail(view);
        //updateStatusCircle(view, isReceiverOn);

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
        String user = DefaultSharedPreferenceManager.getUserEmail(getActivity());
        String token = DefaultSharedPreferenceManager.getUserToken(getActivity());
        if (user != null && token != null) {
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
        if (resultCode == RESULT_OK) {
            if (requestCode == AUTHORIZATION_CODE) {
                requestToken();
            } else if (requestCode == ACCOUNT_CODE) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                DefaultSharedPreferenceManager.setUserEmail(getActivity(), accountName);
                invalidateToken();
                requestToken();
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
        Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{"com.google"}, null, null, null, null);
        startActivityForResult(intent, ACCOUNT_CODE);
    }

    public void updateConfiguredEmail(View view) {
        if (DefaultSharedPreferenceManager.getUserEmail(getActivity()) != null && DefaultSharedPreferenceManager.getUserToken(getActivity()) != null) {
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
        DefaultSharedPreferenceManager.setUserToken(getActivity(), null);
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

        //Potentially leaking activity here! Find some other context to pass
        if(isChecked) SchedulingModeBroadcastReceiver.startAlarm(getActivity());
        else SchedulingModeBroadcastReceiver.cancelAlarm(getActivity());
        updateStatusCircle(getView(), switchServiceStatus.isChecked());
    }

    private void updateStatusCircle(View view, boolean serviceOn) {
        ImageView statusCircle = view.findViewById(R.id.statusCircle);
        TextView labelStatus = view.findViewById(R.id.labelStatus);
        TextView labelScheduleTime = view.findViewById(R.id.labelScheduleTime);
        boolean schedulingModeOn = DefaultSharedPreferenceManager.getSchedulingMode(getActivity());
        boolean currentlyScheduled = DefaultSharedPreferenceManager.getCurrentlyScheduled(getActivity());

        if (DefaultSharedPreferenceManager.getUserEmail(getActivity()) == null || DefaultSharedPreferenceManager.getUserToken(getActivity()) == null) {
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
            DayOfWeek dayOfWeekEnum = DayOfWeek.of(dayOfWeek == 1 ? 7 : dayOfWeek - 1);
            String scheduledTime = DefaultSharedPreferenceManager.getSchedule(getActivity(), DefaultSharedPreferenceManager.DAY_OF_THE_WEEK_KEYS[dayOfWeek == 1 ? 6 : dayOfWeek - 2]);
            labelScheduleTime.setText(dayOfWeekEnum.getDisplayName(TextStyle.FULL, Locale.getDefault()) + ", " + scheduledTime);
        } else labelScheduleTime.setText(getResources().getString(R.string.label_schedule_time_off_text));
    }

    private void invalidateToken() {
        AccountManager accountManager = AccountManager.get(getActivity());
        accountManager.invalidateAuthToken("com.google", DefaultSharedPreferenceManager.getUserToken(getActivity()));
        DefaultSharedPreferenceManager.setUserToken(getActivity(), null);
    }

    private void requestToken() {
        Account userAccount = null;
        AccountManager accountManager = AccountManager.get(getActivity());
        String user = DefaultSharedPreferenceManager.getUserEmail(getActivity());
        for (Account account : accountManager.getAccountsByType("com.google")) {
            if (account.name.equals(user)) {
                userAccount = account;
                break;
            }
        }
        accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, getActivity(), new OnTokenAcquired(), null);
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                Bundle bundle = result.getResult();
                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, AUTHORIZATION_CODE);
                } else {
                    String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    DefaultSharedPreferenceManager.setUserToken(getActivity(), token);
                    updateConfiguredEmail(getView());
                    updateStatusCircle(getView(), Util.isSMSMissedCallBroadcastReceiverOn(getActivity()));
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
                deleteConfiguredEmail(getView());
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