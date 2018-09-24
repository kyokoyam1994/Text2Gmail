package com.example.kosko.text2gmail.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.kosko.text2gmail.database.entity.RefreshToken;

import java.util.List;

@Dao
public interface RefreshTokenDao {

    @Query("SELECT * FROM RefreshToken WHERE emailAddress = :email")
    List<RefreshToken> getRefreshTokenByEmail(String email);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insert(RefreshToken refreshToken);

}
