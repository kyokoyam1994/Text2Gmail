package com.example.kosko.text2gmail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class DailySchedulerActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daily_scheduler_activity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                break;
        }
    }

}
