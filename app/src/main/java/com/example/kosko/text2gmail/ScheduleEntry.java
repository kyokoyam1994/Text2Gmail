package com.example.kosko.text2gmail;

import java.time.DayOfWeek;

public class ScheduleEntry {

    private DayOfWeek dayOfWeek;
    private String startTime;
    private String endTime;

    public ScheduleEntry(DayOfWeek dayOfWeek, String startTime, String endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}
