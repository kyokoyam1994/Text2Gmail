package com.example.kosko.text2gmail.adapter;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

public class ContactSelectionAdapter extends SimpleCursorAdapter {

    public ContactSelectionAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

}
