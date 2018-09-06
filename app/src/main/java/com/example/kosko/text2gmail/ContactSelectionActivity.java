package com.example.kosko.text2gmail;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.example.kosko.text2gmail.adapter.ContactSelectionAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ContactSelectionActivity extends AppCompatActivity implements ContactSelectionAdapter.ContactAddedListener, View.OnClickListener {

    public final static String SELECTED_CONTACTS_LIST = "SELECTED_CONTACTS_LIST";
    private ContactSelectionAdapter contactSelectionAdapter;
    private Set<String> blockedContactsSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_selection);

        blockedContactsSet = new HashSet<>();
        Button buttonOK = findViewById(R.id.buttonOK);
        Button buttonCancel = findViewById(R.id.buttonCancel);
        ListView listViewContacts = findViewById(R.id.listViewContacts);

        buttonOK.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, null, null, null);
        contactSelectionAdapter = new ContactSelectionAdapter(this, cursor, 0);
        listViewContacts.setAdapter(contactSelectionAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public void onContactAdded(String contactNumber) {
        if(!blockedContactsSet.contains(contactNumber)) {
            blockedContactsSet.add(contactNumber);
            Chip chip = new Chip(this);
            chip.setChipText(contactNumber);
            chip.setCloseIconEnabled(true);
            chip.setOnCloseIconClickListener((view) -> removeChip(view, contactNumber));
            ChipGroup chipGroupSelectedContacts = findViewById(R.id.chipGroupSelectedContacts);
            chipGroupSelectedContacts.addView(chip);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonOK:
                setActivityResult(false);
                break;
            case R.id.buttonCancel:
                setActivityResult(true);
                break;
        }
    }

    public void setActivityResult(boolean cancelled) {
        if (cancelled) setResult(Activity.RESULT_CANCELED);
        else {
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra(SELECTED_CONTACTS_LIST, new ArrayList<>(blockedContactsSet));
            setResult(Activity.RESULT_OK, resultIntent);
        }
        finish();
    }

    private void removeChip(View view, String contactNumber){
        blockedContactsSet.remove(contactNumber);
        ((ViewGroup) view.getParent()).removeView(view);
    }

}
