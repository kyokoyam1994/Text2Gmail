package com.example.kosko.text2gmail.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.kosko.text2gmail.ContactSelectionActivity;
import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.database.AppDatabase;
import com.example.kosko.text2gmail.database.entity.BlockedContact;
import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;

import java.util.ArrayList;

public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int RC_CONTACT_MANUAL = 101;
    private static final int RC_CONTACT_FROM_BOOK = 201;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        CheckBox checkBoxMissedCalls = view.findViewById(R.id.checkBoxMissedCalls);
        Button buttonBlockContactsManual = view.findViewById(R.id.buttonBlockContactsManual);
        Button buttonBlockContactsFromBook = view.findViewById(R.id.buttonBlockContactsFromBook);

        checkBoxMissedCalls.setChecked(DefaultSharedPreferenceManager.getForwardMissedCalls(getActivity()));

        checkBoxMissedCalls.setOnCheckedChangeListener(this);
        buttonBlockContactsManual.setOnClickListener(this);
        buttonBlockContactsFromBook.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CONTACT_MANUAL) {
            String blockedNumber = data.getStringExtra(ContactsManualDialogFragment.BLOCKED_CONTACT_MANUAL_KEY);
        } else if(requestCode == RC_CONTACT_FROM_BOOK) {
            //Handle Contact From Book
            if (resultCode == Activity.RESULT_OK && data != null) {
                ArrayList<String> selectedContacts = data.getStringArrayListExtra(ContactSelectionActivity.SELECTED_CONTACTS_LIST);
                ArrayList<BlockedContact> blockedContacts = new ArrayList<>();
                for (String contact : selectedContacts) blockedContacts.add(new BlockedContact(contact));
                //Add to database
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (String number : selectedContacts) AppDatabase.getInstance(getActivity()).blockedContactDao().insertAll(blockedContacts);
                    }
                }).start();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonBlockContactsManual:
                blockContactsManually();
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

    public void blockContactsManually(){
        ContactsManualDialogFragment dialog = ContactsManualDialogFragment.newInstance();
        dialog.setTargetFragment(this, RC_CONTACT_MANUAL);
        dialog.show(getActivity().getSupportFragmentManager(), "Manual Contacts");
    }

    public void blockContactsFromBook(){
        Intent intent = new Intent(getActivity(), ContactSelectionActivity.class);
        startActivityForResult(intent, RC_CONTACT_FROM_BOOK);
    }

    public void toggleForwardMissedCalls(boolean isChecked) {
        DefaultSharedPreferenceManager.setForwardMissedCalls(getActivity(), isChecked);
    }

}