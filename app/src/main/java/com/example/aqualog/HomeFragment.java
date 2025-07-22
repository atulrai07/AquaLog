package com.example.aqualog;

import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aqualog.data.WaterDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final int WATER_GOAL_ML = 4000;  // Updated daily goal
    private static final int REQUEST_EXACT_ALARM = 5001;

    private TextView tvGreeting, tvLogTime, tvWaterLevel, tvProgressText, tvPercentage;
    private ProgressBar progressBar;
    private Button btnAddNow, btnReset;
    private GridLayout gridStreak;
    private int lastProgress = 0;
    private Handler midnightHandler = new Handler();

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvLogTime = view.findViewById(R.id.tvLogTime);
        tvWaterLevel = view.findViewById(R.id.tvWaterLevel);
        tvProgressText = view.findViewById(R.id.tvProgressText);
        tvPercentage = view.findViewById(R.id.tvPercentage);
        progressBar = view.findViewById(R.id.progressBar);
        btnAddNow = view.findViewById(R.id.btnAddNow);
        btnReset = view.findViewById(R.id.btnReset);
        gridStreak = view.findViewById(R.id.gridStreak);

        tvGreeting.setText("Hi Atul 👋");

        btnAddNow.setOnClickListener(v -> {
            Context context = getContext();
            if (context != null) {
                startActivity(new Intent(context, AddNowActivity.class));
            }
        });

        btnReset.setOnClickListener(v -> resetTodayLogs());

        updateProgressUI();
        populateStreakGrid();
        scheduleMidnightRefresh();
        checkAndScheduleReminders();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProgressUI();
        populateStreakGrid();
        scheduleMidnightRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        midnightHandler.removeCallbacksAndMessages(null);
    }

    private void checkAndScheduleReminders() {
        Context context = getContext();
        if (context == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Please allow exact alarms for water reminders.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }
        scheduleWaterReminders();
    }

    private void scheduleWaterReminders() {
        scheduleReminder(15, 0, 1001);
        scheduleReminder(19, 0, 1002);
        scheduleReminder(21, 0, 1003);
    }

    private void scheduleReminder(int hour, int minute, int requestCode) {
        Context context = getContext();
        if (context == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, WaterReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } catch (SecurityException e) {
                Toast.makeText(context, "Cannot schedule reminder: Missing permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Midnight reset: Resets progress daily and streak grid only on Sundays.
     */
    private void scheduleMidnightRefresh() {
        midnightHandler.removeCallbacksAndMessages(null);
        long now = System.currentTimeMillis();

        Calendar nextMidnight = Calendar.getInstance();
        nextMidnight.add(Calendar.DAY_OF_YEAR, 1);
        nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextMidnight.set(Calendar.MINUTE, 0);
        nextMidnight.set(Calendar.SECOND, 0);
        nextMidnight.set(Calendar.MILLISECOND, 0);

        long delay = nextMidnight.getTimeInMillis() - now;

        midnightHandler.postDelayed(() -> {
            if (!isAdded()) return;
            updateProgressUI(); // Reset daily progress

            Calendar today = Calendar.getInstance();
            if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                resetStreakGridForNewWeek();
            }

            scheduleMidnightRefresh(); // Reschedule for next midnight
        }, delay);
    }

    /**
     * Clears the streak grid at the start of a new week (Sunday midnight).
     */
    private void resetStreakGridForNewWeek() {
        requireActivity().runOnUiThread(() -> {
            gridStreak.removeAllViews();
            Toast.makeText(getContext(), "New week started. Streak reset!", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateProgressUI() {
        Context context = getContext();
        if (context == null) return;

        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();

            int totalMl = WaterDatabase.getInstance(context)
                    .waterLogDao()
                    .getTotalForDay(startOfDay);

            long lastLogTime = WaterDatabase.getInstance(context)
                    .waterLogDao()
                    .getLastLogTime();

            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                if (lastLogTime != 0) {
                    String formattedTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(new Date(lastLogTime));
                    tvLogTime.setText(formattedTime);
                } else {
                    tvLogTime.setText("No log yet");
                }

                tvProgressText.setText(totalMl + "ml of " + WATER_GOAL_ML + "ml");
                tvWaterLevel.setText("Progress: " + totalMl + " ml");

                int percent = (int) ((totalMl / (float) WATER_GOAL_ML) * 100);
                animateProgressBar(percent);
                tvPercentage.setText(percent + "%");
            });
        }).start();
    }

    private void animateProgressBar(int newProgress) {
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", lastProgress, newProgress);
        animator.setDuration(700);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
        lastProgress = newProgress;
    }

    private void resetTodayLogs() {
        Context context = getContext();
        if (context == null) return;

        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfDay = calendar.getTimeInMillis();

            WaterDatabase.getInstance(context)
                    .waterLogDao()
                    .deleteLogsFromDay(startOfDay);

            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                updateProgressUI();
                Toast.makeText(getContext(), "Today's progress reset!", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void populateStreakGrid() {
        Context context = getContext();
        if (context == null) return;

        new Thread(() -> {
            Calendar calendar = Calendar.getInstance();
            String[] days = new String[7];
            int[] dailyTotals = new int[7];

            for (int i = 6; i >= 0; i--) {
                Calendar dayCal = (Calendar) calendar.clone();
                dayCal.add(Calendar.DAY_OF_YEAR, -i);
                dayCal.set(Calendar.HOUR_OF_DAY, 0);
                dayCal.set(Calendar.MINUTE, 0);
                dayCal.set(Calendar.SECOND, 0);
                dayCal.set(Calendar.MILLISECOND, 0);
                long startOfDay = dayCal.getTimeInMillis();

                dailyTotals[6 - i] = WaterDatabase.getInstance(context)
                        .waterLogDao()
                        .getTotalForDay(startOfDay);

                days[6 - i] = new SimpleDateFormat("EEE", Locale.getDefault())
                        .format(new Date(startOfDay));
            }

            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                gridStreak.removeAllViews();

                int todayIndex = 6;
                for (int i = 0; i < 7; i++) {
                    TextView dayView = new TextView(getContext());
                    dayView.setText(days[i]);
                    dayView.setGravity(Gravity.CENTER);
                    dayView.setTextColor(Color.WHITE);
                    dayView.setTextSize(14);

                    if (i == todayIndex && dailyTotals[i] >= WATER_GOAL_ML) {
                        dayView.setBackgroundColor(Color.parseColor("#2196F3"));
                    } else {
                        dayView.setBackgroundColor(Color.GRAY);
                    }

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.columnSpec = GridLayout.spec(i % 7, 1f);
                    params.setMargins(8, 8, 8, 8);
                    dayView.setLayoutParams(params);
                    dayView.setPadding(16, 16, 16, 16);

                    gridStreak.addView(dayView);

                    Animation scaleAnim = new ScaleAnimation(
                            0.8f, 1f, 0.8f, 1f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f
                    );
                    scaleAnim.setDuration(300);
                    dayView.startAnimation(scaleAnim);
                }
            });
        }).start();
    }
}
