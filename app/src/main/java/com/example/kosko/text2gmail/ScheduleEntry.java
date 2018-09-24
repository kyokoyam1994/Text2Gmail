package com.example.kosko.text2gmail;

public class ScheduleEntry {

    //Alternative to DayOfWeek enum for Android SDK versions that don't have access to Java 8
    public enum DayOfTheWeek {
        MONDAY (1, "Monday"),
        TUESDAY (2, "Tuesday"),
        WEDNESDAY (3, "Wednesday"),
        THURSDAY (4, "Thursday"),
        FRIDAY (5, "Friday"),
        SATURDAY (6, "Saturday"),
        SUNDAY (7, "Sunday");

        private int value;
        private String name;

        DayOfTheWeek(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public static DayOfTheWeek from(int value) {
            switch (value) {
                case 1:
                    return MONDAY;
                case 2:
                    return TUESDAY;
                case 3:
                    return WEDNESDAY;
                case 4:
                    return THURSDAY;
                case 5:
                    return FRIDAY;
                case 6:
                    return SATURDAY;
                case 7:
                    return SUNDAY;
                default:
                    return null;
            }
        }
    }

    private DayOfTheWeek dayOfTheWeek;
    private String startTime;
    private String endTime;

    public ScheduleEntry(DayOfTheWeek dayOfTheWeek, String startTime, String endTime) {
        this.dayOfTheWeek = dayOfTheWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DayOfTheWeek getDayOfTheWeek() {
        return dayOfTheWeek;
    }
    public void setDayOfTheWeek(DayOfTheWeek dayOfWeek) {
        this.dayOfTheWeek = dayOfWeek;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
