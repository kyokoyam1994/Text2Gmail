package com.example.kosko.text2gmail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.kosko.text2gmail.adapter.ScheduleEntryAdapter;
import com.example.kosko.text2gmail.fragment.TimePickerDialogFragment;
import com.example.kosko.text2gmail.util.DefaultSharedPreferenceManager;

import java.time.DayOfWeek;
import java.util.ArrayList;

public class DailySchedulerActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener{

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
        refreshSchedule();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonApply:
                applySelection();
                break;
            case R.id.buttonOK:
                confirmSelection();
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        switch (view.getId()){
            case R.id.editTextStartTime:
                if(hasFocus) TimePickerDialogFragment.newInstance(R.string.start_time_label_text).show(getSupportFragmentManager(), "Start");
                break;
            case R.id.editTextEndTime:
                if(hasFocus) TimePickerDialogFragment.newInstance(R.string.end_time_label_text).show(getSupportFragmentManager(), "End");
                break;
        }
    }

    private void confirmSelection(){

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
        //findViewById(R.id.checkBoxMon).isSelected();
        //findViewById(R.id.checkBoxTue).isSelected();
        //findViewById(R.id.checkBoxWed).isSelected();
        //findViewById(R.id.checkBoxThu).isSelected();
        //findViewById(R.id.checkBoxFri).isSelected();
        //findViewById(R.id.checkBoxSat).isSelected();
        //findViewById(R.id.checkBoxSun).isSelected();
        refreshSchedule();
    }

}