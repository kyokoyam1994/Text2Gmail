package com.example.kosko.text2gmail.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DatabaseTypeConverter {

    @TypeConverter
    public static Date timestampToDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
