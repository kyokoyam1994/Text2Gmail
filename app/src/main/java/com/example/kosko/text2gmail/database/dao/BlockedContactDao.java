package com.example.kosko.text2gmail.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.kosko.text2gmail.database.entity.BlockedContact;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Dao
public interface BlockedContactDao {

    @Query("SELECT * FROM BlockedContact")
    List<BlockedContact> getAll();

    @Insert
    void insert(BlockedContact blockedContact);

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    void insertAll(ArrayList<BlockedContact> blockedContacts);

    @Delete
    void delete(BlockedContact blockedContact);

    @Delete
    void deleteAll(ArrayList<BlockedContact> blockedContacts);

}
