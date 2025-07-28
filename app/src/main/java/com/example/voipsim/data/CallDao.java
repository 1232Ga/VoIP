package com.example.voipsim.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CallDao {
    @Insert
    long insert(CallLog log);

    @Query("SELECT * FROM CallLog ORDER BY startTime DESC")
    List<CallLog> getAll();
}
