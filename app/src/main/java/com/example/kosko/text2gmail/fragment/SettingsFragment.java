package com.example.kosko.text2gmail.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.kosko.text2gmail.ContactSelectionActivity;
import com.example.kosko.text2gmail.receiver.MissedCallBroadcastReceiver;
import com.example.kosko.text2gmail.R;

public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        CheckBox checkBoxMissedCalls = view.findViewById(R.id.checkBoxMissedCalls);
        Button buttonBlockContactsFromBook = view.findViewById(R.id.buttonBlockContactsFromBook);
        checkBoxMissedCalls.setOnCheckedChangeListener(this);
        buttonBlockContactsFromBook.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonBlockContactsManual:
                break;
            case R.id.buttonBlockContactsFromBook:
                blockContactsFromBook();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.checkBoxMissedCalls:
                toggleForwardMissedCalls(isChecked);
                break;
        }
    }

    public void blockContactsFromBook(){
        Intent i = new Intent(getActivity(), ContactSelectionActivity.class);
        startActivity(i);
    }

    public void toggleForwardMissedCalls(boolean isChecked) {
        PackageManager packageManager = getActivity().getPackageManager();
        ComponentName componentName = new ComponentName(getActivity(), MissedCallBroadcastReceiver.class);
        int state = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        if (isChecked) state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        packageManager.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
    }

}
