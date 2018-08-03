package com.example.kosko.text2gmail.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity
public class LogEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String senderNumber;
    private String message;
    private Date dateReceived;
    private boolean sendSuccessful;

    public LogEntry(String senderNumber, String message, Date dateReceived, boolean sendSuccessful) {
        this.senderNumber = senderNumber;
        this.message = message;
        this.dateReceived = dateReceived;
        this.sendSuccessful = sendSuccessful;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(String senderNumber) {
        this.senderNumber = senderNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }

    public boolean isSendSuccessful() {
        return sendSuccessful;
    }

    public void setSendSuccessful(boolean sendSuccessful) {
        this.sendSuccessful = sendSuccessful;
    }

}
