package com.example.aqualog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
                        adapter = new HistoryAdapter(logs);
                        recyclerView.setAdapter(adapter);
                    } else {
                        adapter.updateData(logs);
                    }
                }
            });
        }).start();
    }

    /**
     * Adapter class for the history RecyclerView.
     */
    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
        private List<WaterLog> logList;

        HistoryAdapter(List<WaterLog> logList) {
            this.logList = logList;
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            WaterLog log = logList.get(position);

            // Use public fields directly
            String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(new Date(log.timestamp));

            String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(new Date(log.timestamp));

            holder.tvDate.setText(date);
            holder.tvTime.setText(time);
            holder.tvAmount.setText(log.amount + " ml");
        }

        @Override
        public int getItemCount() {
            return logList.size();
        }

        void updateData(List<WaterLog> newLogs) {
            this.logList = newLogs;
            notifyDataSetChanged();
        }

        static class HistoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvTime, tvAmount;

            HistoryViewHolder(View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvAmount = itemView.findViewById(R.id.tvAmount);
            }
        }
    }
}
