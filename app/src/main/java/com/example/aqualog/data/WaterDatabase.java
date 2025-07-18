package com.example.aqualog.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {WaterLog.class}, version = 1, exportSchema = false)
public abstract class WaterDatabase extends RoomDatabase {

    private static WaterDatabase instance;

    public abstract WaterLogDao waterLogDao();

    public static synchronized WaterDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            WaterDatabase.class,
                            "water_log_db"
                    ).fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
