package com.example.kosko.text2gmail.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.kosko.text2gmail.R;

public class ContactsManualDialogFragment extends AppCompatDialogFragment {

    public static final String BLOCKED_CONTACT_MANUAL_KEY = "BLOCKED_CONTACT_MANUAL_KEY";
    private EditText editTextEmailAddress;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.contacts_manual_dialog_fragment, null);

        editTextEmailAddress = view.findViewById(R.id.editTextBlockedContact);

        builder.setTitle("Blocked Contact")
            .setView(view)
            .setNegativeButton("Cancel", (dialog, which) -> {})
            .setPositiveButton("OK", (dialog, which) -> {
                Intent intent = new Intent();
                intent.putExtra(BLOCKED_CONTACT_MANUAL_KEY, editTextEmailAddress.getText().toString());
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            });
        return builder.create();
    }

    public static ContactsManualDialogFragment newInstance(){
        return new ContactsManualDialogFragment();
    }

}
