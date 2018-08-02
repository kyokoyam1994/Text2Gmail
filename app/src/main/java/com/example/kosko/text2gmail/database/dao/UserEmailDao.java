package com.example.kosko.text2gmail.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.example.kosko.text2gmail.database.entity.UserEmail;

import java.util.List;

@Dao
public interface UserEmailDao {

    @Query("SELECT * FROM UserEmail")
    List<UserEmail> getAll();

}
