package com.example.kosko.text2gmail.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.kosko.text2gmail.ContactSelectionActivity;
import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.adapter.BlockedContactAdapter;
import com.example.kosko.text2gmail.database.AppDatabase;
import com.example.kosko.text2gmail.database.entity.BlockedContact;
import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;
import com.example.kosko.text2gmail.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String INSERT_OPERATION = "INSERT_OPERATION";
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
        refreshBlockedContacts();
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
                new Thread(() -> {
                    AppDatabase.getInstance(getActivity()).blockedContactDao().insertAll(blockedContacts);
                    refreshBlockedContacts();
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


    public void refreshBlockedContacts(){ new BlockedContactTask().execute(); }

    private class BlockedContactTask extends AsyncTask<Void, Void, List<BlockedContact>> {
        private HashMap<String, String> contactNameMap = new HashMap<>();

        @Override
        protected List<BlockedContact> doInBackground(Void... voids) {
            List<BlockedContact> blockedContacts = AppDatabase.getInstance(getActivity()).blockedContactDao().getAll();
            for (BlockedContact contact : blockedContacts) contactNameMap.put(contact.getBlockedNumber(), Util.findContactNameByNumber(getActivity(), contact.getBlockedNumber()));
            return blockedContacts;
        }

        @Override
        protected void onPostExecute(List<BlockedContact> blockedContacts) {
            BlockedContactAdapter adapter = new BlockedContactAdapter(blockedContacts, contactNameMap);
            RecyclerView recyclerViewBlockedContacts = getView().findViewById(R.id.recyclerViewBlockedContacts);
            recyclerViewBlockedContacts.setAdapter(adapter);
            recyclerViewBlockedContacts.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }

}