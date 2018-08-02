package com.example.kosko.text2gmail.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class BlockedContact {

    @PrimaryKey
    @NonNull
    private String blockedNumber;

    public String getBlockedNumber() {
        return blockedNumber;
    }

    public void setBlockedNumber(String blockedNumber) {
        this.blockedNumber = blockedNumber;
    }

}
