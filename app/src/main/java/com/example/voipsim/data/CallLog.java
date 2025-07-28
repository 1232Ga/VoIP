package com.example.voipsim.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CallLog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String caller;
    public long startTime;
    public long endTime;
    public String type; // Missed / Answered
    public long duration; // ms
}
