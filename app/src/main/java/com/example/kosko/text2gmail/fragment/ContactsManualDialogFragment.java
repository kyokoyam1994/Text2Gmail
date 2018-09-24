package com.example.kosko.text2gmail.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();

        //Override positive button to prevent closing
        dialog.setOnShowListener(dialogInterface -> {
            Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button buttonNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            buttonPositive.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            buttonNegative.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            buttonPositive.setOnClickListener(view1 -> {
                TextView textViewErrorMessage = view.findViewById(R.id.textViewErrorMessage);
                if (PhoneNumberUtils.isGlobalPhoneNumber(editTextEmailAddress.getText().toString())) {
                    textViewErrorMessage.setText("");
                    Intent intent = new Intent();
                    intent.putExtra(BLOCKED_CONTACT_MANUAL_KEY, editTextEmailAddress.getText().toString());
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    dismiss();
                } else textViewErrorMessage.setText(R.string.blocked_contacts_manual_dialog_error_text);
            });
        });

        return dialog;
    }

    public static ContactsManualDialogFragment newInstance(){
        return new ContactsManualDialogFragment();
    }

}
