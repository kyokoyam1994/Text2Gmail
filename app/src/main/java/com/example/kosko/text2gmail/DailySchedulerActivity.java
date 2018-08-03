package com.example.kosko.text2gmail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TimePicker;

public class DailySchedulerActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daily_scheduler_activity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonApply:
                applySelection();
                break;
        }
    }

    private void applySelection(){
        //findViewById(R.id.checkBoxMon).isSelected();
        //findViewById(R.id.checkBoxTue).isSelected();
        //findViewById(R.id.checkBoxWed).isSelected();
        //findViewById(R.id.checkBoxThu).isSelected();
        //findViewById(R.id.checkBoxFri).isSelected();
        //findViewById(R.id.checkBoxSat).isSelected();
        //findViewById(R.id.checkBoxSun).isSelected();

        TimePicker timePicker = findViewById(R.id.timePickerStartTime);
        //timePicker.getHour();
        //timePicker.getMinute();
    }

}
