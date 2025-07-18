package com.example.aqualog.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_logs")
public class WaterLog {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp; // Time of log (in millis)
    public int amount;     // Amount in ml

    public WaterLog(long timestamp, int amount) {
        this.timestamp = timestamp;
        this.amount = amount;
    }
}
