package com.example.kosko.text2gmail.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import com.example.kosko.text2gmail.R;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class TimePickerDialogFragment extends AppCompatDialogFragment{

    private static final String TITLE_KEY = "TITLE_KEY";
    private static final String TIME_KEY = "TIME_KEY";
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

        try {
            DateTimeFormatter parseFormat = new DateTimeFormatterBuilder().appendPattern("h:mma").toFormatter();
            LocalTime localTime = LocalTime.parse(time, parseFormat);
            timePickerSchedule.setHour(localTime.getHour());
            timePickerSchedule.setMinute(localTime.getMinute());
        } catch (Exception e) {
            timePickerSchedule.setHour(0);
            timePickerSchedule.setMinute(0);
        }

        builder.setTitle(title)
            .setMessage(R.string.time_picker_dialog_fragment_message)
            .setView(view)
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timeSelectedListener.onTimeSelected(title, timePickerSchedule.getHour(), timePickerSchedule.getMinute(), true);
                }
            })
            .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timeSelectedListener.onTimeSelected(title, timePickerSchedule.getHour(), timePickerSchedule.getMinute(), false);
                }
            });
        return builder.create();
    }

    public static TimePickerDialogFragment newInstance(int title, String time){
        TimePickerDialogFragment instance = new TimePickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE_KEY, title);
        args.putString(TIME_KEY, time);
        instance.setArguments(args);
        return instance;
    }

}
