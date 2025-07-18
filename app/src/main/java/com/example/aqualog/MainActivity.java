package com.example.aqualog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aqualog.data.WaterDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int WATER_GOAL_ML = 2000;

    private TextView tvGreeting, tvLogTime, tvWaterLevel, tvProgressText, tvPercentage;
    private ProgressBar progressBar;
    private Button btnAddNow, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View bindings
        tvGreeting = findViewById(R.id.tvGreeting);
        tvLogTime = findViewById(R.id.tvLogTime);
        tvWaterLevel = findViewById(R.id.tvWaterLevel);
        tvProgressText = findViewById(R.id.tvProgressText);
        tvPercentage = findViewById(R.id.tvPercentage);
        progressBar = findViewById(R.id.progressBar);
        btnAddNow = findViewById(R.id.btnAddNow);
        btnReset = findViewById(R.id.btnReset);

        // Greeting
        tvGreeting.setText("Hi Atul 👋");

        // Add Now Button
        btnAddNow.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddNowActivity.class);
            startActivity(intent);
        });

        // Reset Button
        btnReset.setOnClickListener(view -> resetTodayLogs());

        // Initial UI load
        updateProgressUI();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_history) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                return true;
            }
            return false;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProgressUI(); // refresh data when returning
    }

    private void updateProgressUI() {
        new Thread(() -> {
            // Get today's start time in millis
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();

            // Query today's total from Room
            int totalMl = WaterDatabase.getInstance(this)
                    .waterLogDao()
                    .getTotalForDay(startOfDay);

            // Get last log time
            long lastLogTime = WaterDatabase.getInstance(this)
                    .waterLogDao()
                    .getLastLogTime();

            runOnUiThread(() -> {
                // Update last log time
                if (lastLogTime != 0) {
                    String formattedTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(new Date(lastLogTime));
                    tvLogTime.setText(formattedTime);
                } else {
                    tvLogTime.setText("No log yet");
                }

                // Update dynamic texts and progress
                tvProgressText.setText(totalMl + "ml of " + WATER_GOAL_ML + "ml");
                tvWaterLevel.setText("Progress: " + totalMl + " ml");

                int percent = (int) ((totalMl / (float) WATER_GOAL_ML) * 100);
                progressBar.setProgress(percent);
                tvPercentage.setText(percent + "%");
            });
        }).start();
    }

    private void resetTodayLogs() {
        new Thread(() -> {
            // Get today's start time
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();

            // Delete today's logs
            WaterDatabase.getInstance(this)
                    .waterLogDao()
                    .deleteLogsFromDay(startOfDay);

            runOnUiThread(() -> {
                updateProgressUI();
                Toast.makeText(MainActivity.this, "Today's progress reset!", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
