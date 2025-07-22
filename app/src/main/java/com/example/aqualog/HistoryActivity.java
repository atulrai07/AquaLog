package com.example.aqualog;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualog.data.WaterDatabase;
import com.example.aqualog.data.WaterLog;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvNoHistory;
    private HistoryAdapter adapter;
    private List<WaterLog> logs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerHistory);
        tvNoHistory = findViewById(R.id.tvNoHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
    }

    private void loadHistory() {
        new Thread(() -> {
            logs = WaterDatabase.getInstance(getApplicationContext())
                    .waterLogDao()
                    .getAllLogs();

            runOnUiThread(() -> {
                if (logs.isEmpty()) {
                    tvNoHistory.setVisibility(TextView.VISIBLE);
                    recyclerView.setVisibility(RecyclerView.GONE);
                } else {
                    tvNoHistory.setVisibility(TextView.GONE);
                    recyclerView.setVisibility(RecyclerView.VISIBLE);
                    adapter = new HistoryAdapter(this, logs);
                    recyclerView.setAdapter(adapter);
                }
            });
        }).start();
    }
}
