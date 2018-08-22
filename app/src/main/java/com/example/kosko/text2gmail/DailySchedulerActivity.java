package com.example.kosko.text2gmail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kosko.text2gmail.adapter.ScheduleEntryAdapter;
import com.example.kosko.text2gmail.fragment.TimePickerDialogFragment;
import com.example.kosko.text2gmail.receiver.SchedulingModeBroadcastReceiver;
import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class DailySchedulerActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnFocusChangeListener, TimePickerDialogFragment.TimeSelectedListener {

    private Button buttonApply;
    private Button buttonOK;
    private EditText editTextStartTime;
    private EditText editTextEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daily_scheduler_activity);

        buttonApply = findViewById(R.id.buttonApply);
        buttonOK = findViewById(R.id.buttonOK);
        editTextStartTime = findViewById(R.id.editTextStartTime);
        editTextEndTime = findViewById(R.id.editTextEndTime);

        buttonApply.setOnClickListener(this);
        buttonOK.setOnClickListener(this);
        editTextStartTime.setOnFocusChangeListener(this);
        editTextEndTime.setOnFocusChangeListener(this);

        Calendar curr = Calendar.getInstance();
        int dayOfWeek = curr.get(Calendar.DAY_OF_WEEK);
        int keyPos = dayOfWeek == 1 ? 6 : dayOfWeek - 2;
        String schedule = DefaultSharedPreferenceManager.getSchedule(this, DefaultSharedPreferenceManager.DAY_OF_THE_WEEK_KEYS[keyPos]);
        String[] intervals = schedule.split("~");

        editTextStartTime.setText(intervals[0]);
        editTextEndTime.setText(intervals[1]);
        refreshSchedule();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonApply:
                applySelection();
                break;
            case R.id.buttonOK:
                finish();
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        switch (view.getId()){
            case R.id.editTextStartTime:
                if(hasFocus) TimePickerDialogFragment.newInstance(R.string.start_time_label_text, editTextStartTime.getText().toString()).show(getSupportFragmentManager(), "Start");
                break;
            case R.id.editTextEndTime:
                if(hasFocus) TimePickerDialogFragment.newInstance(R.string.end_time_label_text, editTextEndTime.getText().toString()).show(getSupportFragmentManager(), "End");
                break;
        }
    }

    @Override
    public void onTimeSelected(int title, int hours, int minutes, boolean cancelled) {
        DateTimeFormatter parseFormat = new DateTimeFormatterBuilder().appendPattern("h:mma").toFormatter();
        String time = parseFormat.format(LocalTime.of(hours, minutes));
        if (title == R.string.start_time_label_text) {
            if (!cancelled) editTextStartTime.setText(time);
            editTextStartTime.clearFocus();
        } else if (title == R.string.end_time_label_text) {
            if (!cancelled) editTextEndTime.setText(time);
            editTextEndTime.clearFocus();
        }
    }

    private void refreshSchedule() {
        ListView listViewSchedule = findViewById(R.id.listViewSchedule);
        ArrayList<ScheduleEntry> entries = new ArrayList<>();
        for (int i = 0; i < DefaultSharedPreferenceManager.DAY_OF_THE_WEEK_KEYS.length; i++) {
            String schedule = DefaultSharedPreferenceManager.getSchedule(this, DefaultSharedPreferenceManager.DAY_OF_THE_WEEK_KEYS[i]);
            String[] intervals = schedule.split("~");
            entries.add(new ScheduleEntry(DayOfWeek.of(i+1), intervals[0], intervals[1]));
        }
        ScheduleEntryAdapter scheduleEntryAdapter = new ScheduleEntryAdapter(this, R.layout.schedule_list_item, entries);
        listViewSchedule.setAdapter(scheduleEntryAdapter);
    }

    private void applySelection(){
        TextView textViewScheduleErrorMessage = findViewById(R.id.textViewScheduleErrorMessage);
        try {
            DateTimeFormatter parseFormat = new DateTimeFormatterBuilder().appendPattern("h:mma").toFormatter();
            LocalTime startTime = LocalTime.parse(editTextStartTime.getText().toString(), parseFormat);
            LocalTime endTime = LocalTime.parse(editTextEndTime.getText().toString(), parseFormat);
            if (!endTime.isAfter(startTime)) {
                textViewScheduleErrorMessage.setText(R.string.time_picker_dialog_fragment_end_not_after_start);
                return;
            }
        } catch (DateTimeParseException e) {
            textViewScheduleErrorMessage.setText(R.string.time_picker_dialog_fragment_invalid_time);
            return;
        }
        textViewScheduleErrorMessage.setText("");

        String timeInterval = editTextStartTime.getText().toString() + "~" + editTextEndTime.getText().toString();
        boolean schedulingOn = DefaultSharedPreferenceManager.getSchedulingMode(this);
        if(schedulingOn) SchedulingModeBroadcastReceiver.cancelAlarm(this);

        CheckBox checkBoxMon = findViewById(R.id.checkBoxMon);
        CheckBox checkBoxTue = findViewById(R.id.checkBoxTue);
        CheckBox checkBoxWed = findViewById(R.id.checkBoxWed);
        CheckBox checkBoxThu = findViewById(R.id.checkBoxThu);
        CheckBox checkBoxFri = findViewById(R.id.checkBoxFri);
        CheckBox checkBoxSat = findViewById(R.id.checkBoxSat);
        CheckBox checkBoxSun = findViewById(R.id.checkBoxSun);

        if (checkBoxMon.isChecked()) DefaultSharedPreferenceManager.setSchedule(this, DefaultSharedPreferenceManager.MONDAY_SCHEDULE_KEY, timeInterval);
        if (checkBoxTue.isChecked()) DefaultSharedPreferenceManager.setSchedule(this, DefaultSharedPreferenceManager.TUESDAY_SCHEDULE_KEY, timeInterval);
        if (checkBoxWed.isChecked()) DefaultSharedPreferenceManager.setSchedule(this, DefaultSharedPreferenceManager.WEDNESDAY_SCHEDULE_KEY, timeInterval);
        if (checkBoxThu.isChecked()) DefaultSharedPreferenceManager.setSchedule(this, DefaultSharedPreferenceManager.THURSDAY_SCHEDULE_KEY, timeInterval);
        if (checkBoxFri.isChecked()) DefaultSharedPreferenceManager.setSchedule(this, DefaultSharedPreferenceManager.FRIDAY_SCHEDULE_KEY, timeInterval);
        if (checkBoxSat.isChecked()) DefaultSharedPreferenceManager.setSchedule(this, DefaultSharedPreferenceManager.SATURDAY_SCHEDULE_KEY, timeInterval);
        if (checkBoxSun.isChecked()) DefaultSharedPreferenceManager.setSchedule(this, DefaultSharedPreferenceManager.SUNDAY_SCHEDULE_KEY, timeInterval);

        refreshSchedule();
        if(schedulingOn) SchedulingModeBroadcastReceiver.startAlarm(this);
    }

}