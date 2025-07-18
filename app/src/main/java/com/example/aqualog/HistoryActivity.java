package com.example.aqualog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualog.data.WaterDatabase;
import com.example.aqualog.data.WaterLog;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvNoHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerHistory);
        tvNoHistory = findViewById(R.id.tvNoHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
    }

    private void loadHistory() {
        AsyncTask.execute(() -> {
            List<WaterLog> logs = WaterDatabase.getInstance(getApplicationContext())
                    .waterLogDao().getAllLogs();

            runOnUiThread(() -> {
                if (logs.isEmpty()) {
                    tvNoHistory.setVisibility(TextView.VISIBLE);
                    recyclerView.setVisibility(RecyclerView.GONE);
                } else {
                    tvNoHistory.setVisibility(TextView.GONE);
                    recyclerView.setAdapter(new HistoryAdapter(logs));
                }
            });
        });
    }
}
