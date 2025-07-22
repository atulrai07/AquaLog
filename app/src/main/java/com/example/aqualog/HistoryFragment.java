package com.example.aqualog;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualog.data.WaterDatabase;
import com.example.aqualog.data.WaterLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvNoData;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerHistory);
        tvNoData = rootView.findViewById(R.id.tvNoData);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadHistoryData();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistoryData();
    }

    /**
     * Loads all history logs from the database and updates the RecyclerView.
     */
    private void loadHistoryData() {
        new Thread(() -> {
            List<WaterLog> logs = WaterDatabase.getInstance(requireContext())
                    .waterLogDao()
                    .getAllLogs(); // Fetch all logs from DB

            requireActivity().runOnUiThread(() -> {
                if (logs.isEmpty()) {
                    tvNoData.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvNoData.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (adapter == null) {
                        adapter = new HistoryAdapter(requireContext(), logs);
                        recyclerView.setAdapter(adapter);

                        adapter.setOnDeleteClickListener((position, log) -> {
                            showDeleteConfirmation(log, position);
                        });
                    } else {
                        adapter.updateData(logs);
                    }
                }
            });
        }).start();
    }

    /**
     * Shows a confirmation dialog before deleting a log.
     */
    private void showDeleteConfirmation(WaterLog log, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this log?")
                .setPositiveButton("Delete", (dialog, which) -> deleteLog(log, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes a log entry from the database and updates the RecyclerView.
     */
    private void deleteLog(WaterLog log, int position) {
        new Thread(() -> {
            WaterDatabase.getInstance(requireContext())
                    .waterLogDao()
                    .deleteLogById(log.id);  // Ensure WaterLog has an 'id' field.

            requireActivity().runOnUiThread(() -> {
                adapter.removeItem(position);
                Toast.makeText(requireContext(), "Log deleted", Toast.LENGTH_SHORT).show();
                if (adapter.getItemCount() == 0) {
                    tvNoData.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            });
        }).start();
    }
}
