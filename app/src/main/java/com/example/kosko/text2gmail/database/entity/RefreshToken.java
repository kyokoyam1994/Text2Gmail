package com.example.kosko.text2gmail.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class RefreshToken {

    @PrimaryKey
    @NonNull
    private String emailAddress;

    private String refreshToken;

    public RefreshToken(@NonNull String emailAddress, String refreshToken) {
        this.emailAddress = emailAddress;
        this.refreshToken = refreshToken;
    }

    @NonNull
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(@NonNull String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
