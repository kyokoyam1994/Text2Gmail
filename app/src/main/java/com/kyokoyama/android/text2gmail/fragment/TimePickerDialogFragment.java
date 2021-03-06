package com.kyokoyama.android.text2gmail.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.kyokoyama.android.text2gmail.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimePickerDialogFragment extends AppCompatDialogFragment {

    private static final String TITLE_KEY = "TITLE_KEY";
    private static final String TIME_KEY = "TIME_KEY";
    private static final String HOUR_KEY = "HOUR_KEY";
    private static final String MINUTE_KEY = "MINUTE_KEY";

    private TimePicker timePickerSchedule;
    private TimeSelectedListener timeSelectedListener;

    public interface TimeSelectedListener {
        void onTimeSelected(int title, int hour, int minutes, boolean cancelled);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            timeSelectedListener = (TimeSelectedListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " must implement" + TimeSelectedListener.class.toString());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.time_picker_dialog, null);

        int title = getArguments().getInt(TITLE_KEY);
        String time = getArguments().getString(TIME_KEY);
        timePickerSchedule = view.findViewById(R.id.timePickerSchedule);

        if(savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePickerSchedule.setHour(savedInstanceState.getInt(HOUR_KEY));
                timePickerSchedule.setMinute(savedInstanceState.getInt(MINUTE_KEY));
            } else {
                timePickerSchedule.setCurrentHour(savedInstanceState.getInt(HOUR_KEY));
                timePickerSchedule.setCurrentMinute(savedInstanceState.getInt(MINUTE_KEY));
            }
        } else {
            try {
                DateFormat format = new SimpleDateFormat("h:mma");
                Date parsedTime = format.parse(time);
                Calendar cal = Calendar.getInstance();
                cal.setTime(parsedTime);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePickerSchedule.setHour(cal.get(Calendar.HOUR_OF_DAY));
                    timePickerSchedule.setMinute(cal.get(Calendar.MINUTE));
                } else {
                    timePickerSchedule.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
                    timePickerSchedule.setCurrentMinute(cal.get(Calendar.MINUTE));
                }
            } catch (ParseException e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePickerSchedule.setHour(0);
                    timePickerSchedule.setMinute(0);
                } else {
                    timePickerSchedule.setCurrentHour(0);
                    timePickerSchedule.setCurrentMinute(0);
                }
            }
        }

        builder.setTitle(title)
            .setMessage(R.string.time_picker_dialog_fragment_message)
            .setView(view)
            .setNegativeButton("Cancel", (dialog, which) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timeSelectedListener.onTimeSelected(title, timePickerSchedule.getHour(), timePickerSchedule.getMinute(), true);
                else timeSelectedListener.onTimeSelected(title, timePickerSchedule.getCurrentHour(), timePickerSchedule.getCurrentMinute(), true);
            })
            .setPositiveButton("OK", (dialog, which) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timeSelectedListener.onTimeSelected(title, timePickerSchedule.getHour(), timePickerSchedule.getMinute(), false);
                else timeSelectedListener.onTimeSelected(title, timePickerSchedule.getCurrentHour(), timePickerSchedule.getCurrentMinute(), false);
            });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button buttonNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            buttonPositive.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            buttonNegative.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        });

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int hour;
        int minute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePickerSchedule.getHour();
            minute = timePickerSchedule.getMinute();
        } else {
            hour = timePickerSchedule.getCurrentHour();
            minute = timePickerSchedule.getCurrentMinute();
        }

        outState.putInt(HOUR_KEY, hour);
        outState.putInt(MINUTE_KEY, minute);
    }

    public static TimePickerDialogFragment newInstance(int title, String time) {
        TimePickerDialogFragment instance = new TimePickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE_KEY, title);
        args.putString(TIME_KEY, time);
        instance.setArguments(args);
        return instance;
    }

}
