package com.kyokoyama.android.text2gmail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kyokoyama.android.text2gmail.adapter.ScheduleEntryAdapter;
import com.kyokoyama.android.text2gmail.fragment.TimePickerDialogFragment;
import com.kyokoyama.android.text2gmail.receiver.SchedulingModeBroadcastReceiver;
import com.kyokoyama.android.text2gmail.util.DefaultSharedPreferenceManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DailySchedulerActivity extends AppCompatActivity implements View.OnClickListener,
        TimePickerDialogFragment.TimeSelectedListener {

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
        editTextStartTime.setOnClickListener(this);
        editTextEndTime.setOnClickListener(this);

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
            case R.id.editTextStartTime:
                TimePickerDialogFragment.newInstance(R.string.start_time_label_text, editTextStartTime.getText().toString()).show(getSupportFragmentManager(), "Start");
                break;
            case R.id.editTextEndTime:
                TimePickerDialogFragment.newInstance(R.string.end_time_label_text, editTextEndTime.getText().toString()).show(getSupportFragmentManager(), "End");
                break;
        }
    }

    @Override
    public void onTimeSelected(int title, int hours, int minutes, boolean cancelled) {
        DateFormat parseFormat = new SimpleDateFormat("h:mma");
        Calendar curr = Calendar.getInstance();
        curr.set(Calendar.HOUR_OF_DAY, hours);
        curr.set(Calendar.MINUTE, minutes);
        String time = parseFormat.format(curr.getTime());

        if (title == R.string.start_time_label_text) {
            if (!cancelled) editTextStartTime.setText(time);
            editTextStartTime.clearFocus();
        } else if (title == R.string.end_time_label_text) {
            if (!cancelled) editTextEndTime.setText(time);
            editTextEndTime.clearFocus();
        }
    }

    private void refreshSchedule() {
        //ListView listViewSchedule = findViewById(R.id.listViewSchedule);
        ArrayList<ScheduleEntry> entries = new ArrayList<>();
        for (int i = 0; i < DefaultSharedPreferenceManager.DAY_OF_THE_WEEK_KEYS.length; i++) {
            String schedule = DefaultSharedPreferenceManager.getSchedule(this, DefaultSharedPreferenceManager.DAY_OF_THE_WEEK_KEYS[i]);
            String[] intervals = schedule.split("~");
            entries.add(new ScheduleEntry(ScheduleEntry.DayOfTheWeek.from(i+1), intervals[0], intervals[1]));
        }
        ScheduleEntryAdapter scheduleEntryAdapter = new ScheduleEntryAdapter(this, R.layout.card_view_schedule, entries);
        //listViewSchedule.setAdapter(scheduleEntryAdapter);
        LinearLayout linearLayoutSchedule = findViewById(R.id.linearLayoutSchedule);
        linearLayoutSchedule.removeAllViews();
        for(int i = 0; i < scheduleEntryAdapter.getCount(); i++) {
            linearLayoutSchedule.addView(scheduleEntryAdapter.getView(i, null, null));
        }
    }

    private void applySelection() {
        TextView textViewScheduleErrorMessage = findViewById(R.id.textViewScheduleErrorMessage);
        try {
            DateFormat format = new SimpleDateFormat("h:mma");
            Date startTime = format.parse(editTextStartTime.getText().toString());
            Date endTime = format.parse(editTextEndTime.getText().toString());
            if (!endTime.after(startTime)) {
                textViewScheduleErrorMessage.setText(R.string.time_picker_dialog_fragment_end_not_after_start);
                return;
            }
        } catch (ParseException e) {
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