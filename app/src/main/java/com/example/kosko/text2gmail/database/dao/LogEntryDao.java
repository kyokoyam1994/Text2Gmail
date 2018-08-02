package com.example.kosko.text2gmail.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.kosko.text2gmail.database.entity.LogEntry;

import java.util.List;

@Dao
public interface LogEntryDao {

    @Query("SELECT * FROM LogEntry")
    List<LogEntry> getAll();

    @Query("SELECT * FROM LogEntry ORDER BY dateReceived")
    List<LogEntry> getAllByTimestamp();

    @Query("SELECT * FROM LogEntry ORDER BY senderName, dateReceived")
    List<LogEntry> getAllBySender();

    @Insert
    void insert(LogEntry entry);

    @Query("DELETE FROM LogEntry")
    void deleteAll();

}
