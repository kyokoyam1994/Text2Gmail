package com.example.kosko.text2gmail.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.kosko.text2gmail.ContactSelectionActivity;
import com.example.kosko.text2gmail.R;
import com.example.kosko.text2gmail.adapter.BlockedContactAdapter;
import com.example.kosko.text2gmail.database.AppDatabase;
import com.example.kosko.text2gmail.database.entity.BlockedContact;
import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;
import com.example.kosko.text2gmail.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        BlockedContactAdapter.BlockedContactListener {

    private static final int RC_CONTACT_MANUAL = 101;
    private static final int RC_CONTACT_FROM_BOOK = 201;

    private enum BlockedContactOperation {
        REFRESH,
        DELETE,
        INSERT
    }

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
    public void onResume() {
        super.onResume();
        refreshBlockedContacts();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<BlockedContact> blockedContacts = new ArrayList<>();
        if (requestCode == RC_CONTACT_MANUAL && resultCode == Activity.RESULT_OK && data != null) {
            String blockedNumber = data.getStringExtra(ContactsManualDialogFragment.BLOCKED_CONTACT_MANUAL_KEY);
            blockedContacts.add(new BlockedContact(blockedNumber));
            insertBlockedContacts(blockedContacts);
        } else if(requestCode == RC_CONTACT_FROM_BOOK && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> selectedContacts = data.getStringArrayListExtra(ContactSelectionActivity.SELECTED_CONTACTS_LIST);
            for (String contact : selectedContacts) blockedContacts.add(new BlockedContact(contact));
            insertBlockedContacts(blockedContacts);
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

    @Override
    public void onBlockedContactDeleted(BlockedContact blockedContact) {
        deleteBlockedContact(blockedContact);
    }

    private void blockContactsManually(){
        ContactsManualDialogFragment dialog = ContactsManualDialogFragment.newInstance();
        dialog.setTargetFragment(this, RC_CONTACT_MANUAL);
        dialog.show(getActivity().getSupportFragmentManager(), "Manual Contacts");
    }

    private void blockContactsFromBook() {
        Intent intent = new Intent(getActivity(), ContactSelectionActivity.class);
        startActivityForResult(intent, RC_CONTACT_FROM_BOOK);
    }

    private void toggleForwardMissedCalls(boolean isChecked) {
        DefaultSharedPreferenceManager.setForwardMissedCalls(getActivity(), isChecked);
    }

    private void insertBlockedContacts(ArrayList<BlockedContact> blockedContacts) {
        new BlockedContactTask(BlockedContactOperation.INSERT, blockedContacts).execute();
    }

    private void deleteBlockedContact(BlockedContact blockedContact) {
        new BlockedContactTask(BlockedContactOperation.DELETE, new ArrayList<>(Arrays.asList(new BlockedContact[]{blockedContact}))).execute();
    }

    private void refreshBlockedContacts() { new BlockedContactTask(BlockedContactOperation.REFRESH, null).execute(); }

    private void toggleBlockedContactsView () {
        RecyclerView recyclerViewBlockedContacts = getView().findViewById(R.id.recyclerViewBlockedContacts);
        TextView textViewEmptyBlockedContacts = getView().findViewById(R.id.textViewEmptyBlockedContacts);
        if (recyclerViewBlockedContacts.getAdapter().getItemCount() > 0) {
            textViewEmptyBlockedContacts.setVisibility(View.INVISIBLE);
            recyclerViewBlockedContacts.setVisibility(View.VISIBLE);
        } else {
            textViewEmptyBlockedContacts.setVisibility(View.VISIBLE);
            recyclerViewBlockedContacts.setVisibility(View.INVISIBLE);
        }
    }

    private class BlockedContactTask extends AsyncTask<Void, Void, List<BlockedContact>> {
        private BlockedContactOperation operation;
        private ArrayList<BlockedContact> blockedContacts;
        private HashMap<String, String> contactNameMap = new HashMap<>();
        private HashMap<String, String> contactImageMap = new HashMap<>();

        BlockedContactTask(BlockedContactOperation operation, ArrayList<BlockedContact> blockedContacts) {
            this.operation = operation;
            this.blockedContacts = blockedContacts;
        }

        @Override
        protected List<BlockedContact> doInBackground(Void... voids) {

            if (operation == BlockedContactOperation.INSERT) AppDatabase.getInstance(getActivity()).blockedContactDao().insertAll(blockedContacts);
            else if (operation == BlockedContactOperation.DELETE) AppDatabase.getInstance(getActivity()).blockedContactDao().deleteAll(blockedContacts);

            List<BlockedContact> blockedContacts = AppDatabase.getInstance(getActivity()).blockedContactDao().getAll();
            for (BlockedContact contact : blockedContacts) {
                contactNameMap.put(contact.getBlockedNumber(), Util.findContactNameByNumber(getActivity(), contact.getBlockedNumber()));
                ContentResolver contentResolver = getActivity().getContentResolver();
                Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI},
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?", new String[]{contact.getBlockedNumber()}, null);

                if(cursor != null && cursor.moveToFirst()) {
                    contactImageMap.put(contact.getBlockedNumber(), cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)));
                    cursor.close();
                }
            }

            blockedContacts.sort((blockedContact, blockedContact2) -> {
                String name = contactNameMap.get(blockedContact.getBlockedNumber()) == null ?  "Unknown" : contactNameMap.get(blockedContact.getBlockedNumber());
                String name2 = contactNameMap.get(blockedContact2.getBlockedNumber()) == null ? "Unknown" : contactNameMap.get(blockedContact2.getBlockedNumber());
                if (name.compareTo(name2) == 0) return blockedContact.getBlockedNumber().compareTo(blockedContact2.getBlockedNumber());
                else return name.compareTo(name2);
            });
            return blockedContacts;
        }

        @Override
        protected void onPostExecute(List<BlockedContact> blockedContacts) {
            BlockedContactAdapter adapter = new BlockedContactAdapter(SettingsFragment.this, blockedContacts, contactNameMap, contactImageMap);
            RecyclerView recyclerViewBlockedContacts = getView().findViewById(R.id.recyclerViewBlockedContacts);
            recyclerViewBlockedContacts.setAdapter(adapter);
            recyclerViewBlockedContacts.setLayoutManager(new LinearLayoutManager(getActivity()));
            toggleBlockedContactsView();
        }
    }

}