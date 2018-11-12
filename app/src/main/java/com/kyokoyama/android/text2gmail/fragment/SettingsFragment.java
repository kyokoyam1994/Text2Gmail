package com.kyokoyama.android.text2gmail.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.kyokoyama.android.text2gmail.ContactSelectionActivity;
import com.kyokoyama.android.text2gmail.R;
import com.kyokoyama.android.text2gmail.adapter.BlockedContactAdapter;
import com.kyokoyama.android.text2gmail.database.AppDatabase;
import com.kyokoyama.android.text2gmail.database.entity.BlockedContact;
import com.kyokoyama.android.text2gmail.util.Constants;
import com.kyokoyama.android.text2gmail.util.DefaultSharedPreferenceManager;
import com.kyokoyama.android.text2gmail.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        BlockedContactAdapter.BlockedContactListener {

    private static final int RC_CONTACT_MANUAL = 101;
    private static final int RC_CONTACT_FROM_BOOK = 201;
    private static final int RC_CONTACT_FROM_BOOK_PERMISSION_GRANTED = 202;
    private static final int RC_MISSED_CALL_PERMISSION_GRANTED = 303;

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RC_CONTACT_FROM_BOOK_PERMISSION_GRANTED:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(getActivity(), ContactSelectionActivity.class);
                    startActivityForResult(intent, RC_CONTACT_FROM_BOOK);
                } else Toast.makeText(getActivity(), "Needs permission for contacts", Toast.LENGTH_SHORT).show();
            case RC_MISSED_CALL_PERMISSION_GRANTED:
                CheckBox checkBoxMissedCalls = getView().findViewById(R.id.checkBoxMissedCalls);
                boolean permissionGranted = false;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) permissionGranted = true;
                else Toast.makeText(getActivity(), "Needs permission for phone calls", Toast.LENGTH_SHORT).show();

                //Disable temporarily to prevent callback
                checkBoxMissedCalls.setOnCheckedChangeListener(null);
                checkBoxMissedCalls.setChecked(permissionGranted);
                checkBoxMissedCalls.setOnCheckedChangeListener(this);

                DefaultSharedPreferenceManager.setForwardMissedCalls(getActivity(), permissionGranted);
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
        if (Util.checkPermission(getActivity(), Constants.PERMISSIONS_CONTACTS)) {
            Intent intent = new Intent(getActivity(), ContactSelectionActivity.class);
            startActivityForResult(intent, RC_CONTACT_FROM_BOOK);
        } else {
            requestPermissions(new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS}, RC_CONTACT_FROM_BOOK_PERMISSION_GRANTED);
        }
    }

    private void toggleForwardMissedCalls(boolean isChecked) {
        if (Util.checkPermission(getActivity(), Constants.PERMISSIONS_PHONE)) {
            DefaultSharedPreferenceManager.setForwardMissedCalls(getActivity(), isChecked);
        } else {
            requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS}, RC_MISSED_CALL_PERMISSION_GRANTED);
        }
    }

    private void insertBlockedContacts(ArrayList<BlockedContact> blockedContacts) {
        new BlockedContactTask(this, BlockedContactOperation.INSERT, blockedContacts).execute();
    }

    private void deleteBlockedContact(BlockedContact blockedContact) {
        new BlockedContactTask(this, BlockedContactOperation.DELETE, new ArrayList<>(Arrays.asList(new BlockedContact[]{blockedContact}))).execute();
    }

    private void refreshBlockedContacts() { new BlockedContactTask(this, BlockedContactOperation.REFRESH, null).execute(); }

    private void toggleBlockedContactsView (View view) {
        RecyclerView recyclerViewBlockedContacts = view.findViewById(R.id.recyclerViewBlockedContacts);
        TextView textViewEmptyBlockedContacts = view.findViewById(R.id.textViewEmptyBlockedContacts);
        if (recyclerViewBlockedContacts.getAdapter().getItemCount() > 0) {
            textViewEmptyBlockedContacts.setVisibility(View.INVISIBLE);
            recyclerViewBlockedContacts.setVisibility(View.VISIBLE);
        } else {
            textViewEmptyBlockedContacts.setVisibility(View.VISIBLE);
            recyclerViewBlockedContacts.setVisibility(View.INVISIBLE);
        }
    }

    private static class BlockedContactTask extends AsyncTask<Void, Void, List<BlockedContact>> {
        private SettingsFragment fragment;
        private Context context;
        private BlockedContactOperation operation;
        private ArrayList<BlockedContact> blockedContacts;
        private HashMap<String, String> contactNameMap = new HashMap<>();
        private HashMap<String, String> contactImageMap = new HashMap<>();

        BlockedContactTask(SettingsFragment fragment, BlockedContactOperation operation, ArrayList<BlockedContact> blockedContacts) {
            this.fragment = fragment;
            this.operation = operation;
            this.blockedContacts = blockedContacts;
            context = fragment.getContext().getApplicationContext();
        }

        @Override
        protected List<BlockedContact> doInBackground(Void... voids) {
            if (operation == BlockedContactOperation.INSERT) AppDatabase.getInstance(context).blockedContactDao().insertAll(blockedContacts);
            else if (operation == BlockedContactOperation.DELETE) AppDatabase.getInstance(context).blockedContactDao().deleteAll(blockedContacts);

            List<BlockedContact> blockedContacts = AppDatabase.getInstance(context).blockedContactDao().getAll();
            for (BlockedContact contact : blockedContacts) {
                contactNameMap.put(contact.getBlockedNumber(), Util.findContactNameByNumber(context, contact.getBlockedNumber()));

                Cursor cursor = null;
                if (Util.checkPermission(context, Constants.PERMISSIONS_CONTACTS)) {
                    ContentResolver contentResolver = context.getContentResolver();
                    cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI},
                            ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?", new String[]{contact.getBlockedNumber()}, null);
                }

                if(cursor != null && cursor.moveToFirst()) {
                    contactImageMap.put(contact.getBlockedNumber(), cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)));
                    cursor.close();
                }
            }

            Collections.sort(blockedContacts, (blockedContact, blockedContact2) -> {
                String name = contactNameMap.get(blockedContact.getBlockedNumber()) == null ?  "Unknown" : contactNameMap.get(blockedContact.getBlockedNumber());
                String name2 = contactNameMap.get(blockedContact2.getBlockedNumber()) == null ? "Unknown" : contactNameMap.get(blockedContact2.getBlockedNumber());
                if (name.compareTo(name2) == 0) return blockedContact.getBlockedNumber().compareTo(blockedContact2.getBlockedNumber());
                else return name.compareTo(name2);
            });
            return blockedContacts;
        }

        @Override
        protected void onPostExecute(List<BlockedContact> blockedContacts) {
            if (fragment != null && fragment.getActivity() != null && !fragment.getActivity().isFinishing() && !fragment.getActivity().isDestroyed()) {
                View view = fragment.getView();
                if (view != null) {
                    BlockedContactAdapter adapter = new BlockedContactAdapter(fragment, blockedContacts, contactNameMap, contactImageMap);
                    RecyclerView recyclerViewBlockedContacts = view.findViewById(R.id.recyclerViewBlockedContacts);
                    recyclerViewBlockedContacts.setAdapter(adapter);
                    recyclerViewBlockedContacts.setLayoutManager(new LinearLayoutManager(fragment.getActivity()));
                    fragment.toggleBlockedContactsView(view);
                }
            }
        }
    }

}