package com.example.kosko.text2gmail.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.example.kosko.text2gmail.database.dao.BlockedContactDao;
import com.example.kosko.text2gmail.database.dao.LogEntryDao;
import com.example.kosko.text2gmail.database.dao.RefreshTokenDao;
import com.example.kosko.text2gmail.database.entity.BlockedContact;
import com.example.kosko.text2gmail.database.entity.LogEntry;
import com.example.kosko.text2gmail.database.entity.RefreshToken;

@Database(entities = {BlockedContact.class, LogEntry.class, RefreshToken.class}, version = 1)
@TypeConverters({DatabaseTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract BlockedContactDao blockedContactDao();
    public abstract LogEntryDao logEntryDao();
    public abstract RefreshTokenDao refreshTokenDao();

    public static AppDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "text2gmail_db").build();
        }
        return instance;
    }

}
