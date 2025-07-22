package com.example.aqualog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualog.data.WaterLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<WaterLog> logs;
    private Context context;
    private OnDeleteClickListener deleteClickListener;

    // Callback interface for delete button
    public interface OnDeleteClickListener {
        void onDeleteClick(int position, WaterLog log);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public HistoryAdapter(Context context, List<WaterLog> logs) {
        this.context = context;
        this.logs = logs;
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
        WaterLog log = logs.get(position);

        // Format date and time
        String dateStr = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date(log.timestamp));
        String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(log.timestamp));

        holder.tvDate.setText(dateStr);
        holder.tvTime.setText(timeStr);
        holder.tvAmount.setText(log.amount + " ml");

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position, log);
            }
        });
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvAmount;
        ImageButton btnDelete;

        HistoryViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
