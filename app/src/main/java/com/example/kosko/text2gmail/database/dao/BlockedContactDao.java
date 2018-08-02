package com.example.kosko.text2gmail.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.example.kosko.text2gmail.database.entity.BlockedContact;

import java.util.List;

@Dao
public interface BlockedContactDao {

    @Query("SELECT * FROM BlockedContact")
    List<BlockedContact> getAll();

}
