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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.kosko.text2gmail.adapter.ContactSelectionAdapter;

import java.util.ArrayList;

public class ContactSelectionActivity extends AppCompatActivity implements ContactSelectionAdapter.ContactListener, View.OnClickListener {

    public final static String SELECTED_CONTACTS_LIST = "SELECTED_CONTACTS_LIST";
    private final static String SEARCH_TEXT = "SEARCH_TEXT";
    private final static String[] CONTACTS_PROJECTION = {ContactsContract.Contacts._ID,
                                                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                                                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                                                        ContactsContract.CommonDataKinds.Phone.NUMBER};

    private ContactSelectionAdapter contactSelectionAdapter;
    private ArrayList<String> blockedContactsList;
    private SearchView searchView;
    private String searchViewText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_selection);

        Button buttonOK = findViewById(R.id.buttonOK);
        Button buttonCancel = findViewById(R.id.buttonCancel);
        ListView listViewContacts = findViewById(R.id.listViewContacts);

        if (savedInstanceState != null) {
            blockedContactsList = savedInstanceState.getStringArrayList(SELECTED_CONTACTS_LIST);
            searchViewText = savedInstanceState.getString(SEARCH_TEXT);
        } else {
            blockedContactsList = new ArrayList<>();
            searchViewText = "";
        }

        for (String blockedContact : blockedContactsList) {
            addChip(blockedContact);
        }

        buttonOK.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);

        Cursor cursor = queryContacts(searchViewText);
        contactSelectionAdapter = new ContactSelectionAdapter(this, cursor, 0);
        listViewContacts.setAdapter(contactSelectionAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SELECTED_CONTACTS_LIST, new ArrayList<>(blockedContactsList));
        outState.putString(SEARCH_TEXT, searchView.getQuery().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Cursor cursor = queryContacts(s);
                contactSelectionAdapter.changeCursor(cursor);
                return false;
            }
        });
        searchView.setQuery(searchViewText, true);
        return true;
    }

    @Override
    public ArrayList<String> getContacts() {
        return blockedContactsList;
    }

    @Override
    public void onContactAdded(String contactNumber) {
        if(!blockedContactsList.contains(contactNumber)) {
            blockedContactsList.add(contactNumber);
            addChip(contactNumber);
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

    private void setActivityResult(boolean cancelled) {
        if (cancelled) setResult(Activity.RESULT_CANCELED);
        else {
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra(SELECTED_CONTACTS_LIST, blockedContactsList);
            setResult(Activity.RESULT_OK, resultIntent);
        }
        finish();
    }

    private void addChip(String contactNumber) {
        Chip chip = new Chip(this);
        chip.setTextAppearanceResource(R.style.ChipStyle);
        chip.setChipBackgroundColorResource(R.color.colorNavy);
        chip.setCloseIconTintResource(R.color.colorWhite);
        Log.d("TAG", String.valueOf(chip.getTextAppearance().textSize));
        chip.setChipText(contactNumber);
        chip.setCloseIconEnabled(true);
        chip.setOnCloseIconClickListener((view) -> removeChip(view, contactNumber));
        ChipGroup chipGroupSelectedContacts = findViewById(R.id.chipGroupSelectedContacts);
        chipGroupSelectedContacts.addView(chip);
    }

    private void removeChip(View view, String contactNumber) {
        blockedContactsList.remove(contactNumber);
        ((ViewGroup) view.getParent()).removeView(view);
        contactSelectionAdapter.notifyDataSetChanged();
    }

    private Cursor queryContacts(String input) {
        String queryString = "%" + input + "%";
        ContentResolver contentResolver = getContentResolver();
        return contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, CONTACTS_PROJECTION,
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?", new String[]{queryString}, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC");
    }

}
