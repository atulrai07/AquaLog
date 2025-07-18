package com.example.aqualog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
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

        // Bind views
        tvGreeting = findViewById(R.id.tvGreeting);
        tvLogTime = findViewById(R.id.tvLogTime);
        tvProgressText = findViewById(R.id.tvProgressText);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvWaterLevel = findViewById(R.id.tvWaterLevel);  // ✅ Missing binding added
        progressBar = findViewById(R.id.progressBar);
        btnAddNow = findViewById(R.id.btnAddNow);
        btnReset = findViewById(R.id.btnReset);

        // Greeting
        tvGreeting.setText("Hi Atul 👋");

        // Add Now button listener
        btnAddNow.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddNowActivity.class);
            startActivity(intent);
        });

        // Reset button listener
        btnReset.setOnClickListener(view -> {
            SharedPreferences prefs = getSharedPreferences("water_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("total_ml", 0);
            editor.putLong("last_time", 0);
            editor.apply();

            updateProgressUI();
            Toast.makeText(MainActivity.this, "Progress reset for the day!", Toast.LENGTH_SHORT).show();
        });

        // Initial UI update
        updateProgressUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProgressUI(); // refresh when returning
    }

    private void updateProgressUI() {
        SharedPreferences prefs = getSharedPreferences("water_prefs", MODE_PRIVATE);
        int totalMl = prefs.getInt("total_ml", 0);
        long lastTime = prefs.getLong("last_time", 0);

        // Last log time
        if (lastTime != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String formattedTime = sdf.format(new Date(lastTime));
            tvLogTime.setText(formattedTime);
        } else {
            tvLogTime.setText("No log yet");
        }

        // Progress display
        tvProgressText.setText(totalMl + "ml of 2000ml");
        tvWaterLevel.setText("Progress: " + totalMl + " ml");

        int percent = (int) ((totalMl / (float) WATER_GOAL_ML) * 100);
        progressBar.setProgress(percent);
        tvPercentage.setText(percent + "%");
    }
}
