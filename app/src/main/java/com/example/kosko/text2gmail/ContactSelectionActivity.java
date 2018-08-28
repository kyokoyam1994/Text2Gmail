package com.example.kosko.text2gmail;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;

import com.example.kosko.text2gmail.adapter.ContactSelectionAdapter;

public class ContactSelectionActivity extends AppCompatActivity{

    private ListView listViewContacts;
    private ContactSelectionAdapter contactSelectionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_selection);

        listViewContacts = findViewById(R.id.listViewContacts);
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

}
