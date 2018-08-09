package com.example.kosko.text2gmail.fragment;

import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.kosko.text2gmail.DailySchedulerActivity;
import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.receiver.SMSMissedCallBroadcastReceiver;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class EmailConfigFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    public enum ServiceStatus {
        RUNNING,
        STOPPED,
        NOT_SCHEDULED,
        NOT_CONFIGURED
    }

    private static final int RC_SIGN_IN = 1001;
    private ServiceStatus serviceStatus = ServiceStatus.NOT_CONFIGURED;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.email_config_fragment, container, false);

        Button setScheduleButton = view.findViewById(R.id.setScheduleButton);
        Switch switchServiceStatus = view.findViewById(R.id.switchServiceStatus);
        ImageButton addEmailButton = view.findViewById(R.id.addEmailButton);

        PackageManager packageManager = getActivity().getPackageManager();
        ComponentName componentName = new ComponentName(getActivity(), SMSMissedCallBroadcastReceiver.class);
        int state = packageManager.getComponentEnabledSetting(componentName);
        switchServiceStatus.setChecked(state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
        updateStatusCircle(view, state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

        setScheduleButton.setOnClickListener(this);
        switchServiceStatus.setOnCheckedChangeListener(this);
        addEmailButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setScheduleButton:
                configureSchedule();
                break;
            case R.id.addEmailButton:
                promptEmail();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switchServiceStatus:
                toggleServiceStatus(isChecked);
                break;
        }
    }

    public void configureSchedule() {
        Intent intent = new Intent(getActivity(), DailySchedulerActivity.class);
        startActivity(intent);
    }

    public void promptEmail(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        }).start();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            System.out.println("Begin handle sign in");
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            AccountManager accountManager = AccountManager.get(getActivity());
            accountManager.getAuthToken(account.getAccount(), "android", null, true, null, null);
            System.out.println("End handle sign in");
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            e.printStackTrace();
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

    private void updateStatusCircle(View view, boolean on){
        ImageView statusCircle = view.findViewById(R.id.statusCircle);
        TextView labelStatus = view.findViewById(R.id.labelStatus);
        if (on){
            statusCircle.getDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorGreen), PorterDuff.Mode.SRC);
            labelStatus.setText(R.string.status_label_text_running);
        } else {
            statusCircle.getDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorRed), PorterDuff.Mode.SRC);
            labelStatus.setText(R.string.status_label_text_stopped);
        }
    }

}
