package com.example.aqualog.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WaterLogDao {

    // Insert a new water log entry
    @Insert
    void insert(WaterLog log);

    // Get total water intake for the current day
    @Query("SELECT SUM(amount) FROM water_logs WHERE timestamp >= :startOfDay")
    int getTotalForDay(long startOfDay);

    // Get the latest log timestamp
    @Query("SELECT MAX(timestamp) FROM water_logs")
    long getLastLogTime();

    // Delete all logs for the current day
    @Query("DELETE FROM water_logs WHERE timestamp >= :startOfDay")
    void deleteLogsFromDay(long startOfDay);

    // Optional: Get all logs (useful for debugging or showing history)
    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    List<WaterLog> getAllLogs();
}
