package com.kyokoyama.android.text2gmail.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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

import com.kyokoyama.android.text2gmail.R;

public class ContactsManualDialogFragment extends AppCompatDialogFragment implements DialogInterface.OnShowListener{

    public static final String BLOCKED_CONTACT_MANUAL_KEY = "BLOCKED_CONTACT_MANUAL_KEY";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.contacts_manual_dialog_fragment, null);

        builder.setTitle("Blocked Contact")
            .setView(inflatedView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(this);
        dialog.setOnDismissListener(this);
        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        //Override positive button to prevent closing
        AlertDialog alertDialog = (AlertDialog) dialogInterface;
        Button buttonNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button buttonPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        buttonNegative.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        buttonPositive.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        buttonPositive.setOnClickListener(view -> {
                Dialog dialog = getDialog();
                TextView textViewErrorMessage = dialog.findViewById(R.id.textViewErrorMessage);
                EditText editTextEmailAddress = dialog.findViewById(R.id.editTextBlockedContact);
                if (PhoneNumberUtils.isGlobalPhoneNumber(editTextEmailAddress.getText().toString())) {
                    textViewErrorMessage.setText("");
                    Intent intent = new Intent();
                    intent.putExtra(BLOCKED_CONTACT_MANUAL_KEY, editTextEmailAddress.getText().toString());
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    dismiss();
                } else textViewErrorMessage.setText(R.string.blocked_contacts_manual_dialog_error_text);
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Button buttonPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        buttonPositive.setOnClickListener(null);
    }

    public static ContactsManualDialogFragment newInstance() {
        return new ContactsManualDialogFragment();
    }

}
