package com.example.aqualog.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_logs")
public class WaterLog {

    @PrimaryKey(autoGenerate = true)
    public int id;           // Unique ID for each log

    public int amount;       // Water amount in ml
    public long timestamp;   // Log timestamp in milliseconds

    // Constructor updated to match usage: (timestamp, amount)
    public WaterLog(long timestamp, int amount) {
        this.timestamp = timestamp;
        this.amount = amount;
    }
}
