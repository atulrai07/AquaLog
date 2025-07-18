package com.example.aqualog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddNowActivity extends AppCompatActivity {

    Button addGlassBtn;
    NumberPicker npGlasses;
    TextView tvCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_now);

        // Initialize views
        addGlassBtn = findViewById(R.id.btnSaveEntry);
        npGlasses = findViewById(R.id.npGlasses);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);

        // Configure NumberPicker
        npGlasses.setMinValue(1);
        npGlasses.setMaxValue(10);

        // Set current time dynamically
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvCurrentTime.setText(currentTime);

        // Button click logic
        addGlassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int glasses = npGlasses.getValue(); // Get selected number of glasses
                int totalAdded = glasses * 250;     // 250ml per glass

                // Get SharedPreferences
                SharedPreferences prefs = getSharedPreferences("water_prefs", MODE_PRIVATE);
                int currentTotal = prefs.getInt("total_ml", 0);
                int updatedTotal = currentTotal + totalAdded;

                // Save new total and timestamp
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("total_ml", updatedTotal);
                editor.putLong("last_time", System.currentTimeMillis());
                editor.apply();

                // Show confirmation
                Toast.makeText(AddNowActivity.this, glasses + " glass(es) added!", Toast.LENGTH_SHORT).show();

                // Close the activity
                finish();
            }
        });
    }
}
