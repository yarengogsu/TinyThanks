package com.example.tinythanks;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private GratitudeViewModel gratitudeViewModel;
    private TextView txtStreakCount;
    private ImageView imgAvatar;

    // Mood Garden
    private MoodGardenView moodGardenView;
    private ImageView imgEmptyMoodPlaceholder;

    // Tiny Tasks
    private EditText etNewTask;
    private ImageView btnAddNewTask;
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bildirim sistemi
        createNotificationChannel();
        askNotificationPermission();
        scheduleDailyNotification();

        // View bağlama
        FloatingActionButton fabMain = findViewById(R.id.fab_add);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        txtStreakCount = findViewById(R.id.txt_streak_count);
        imgAvatar = findViewById(R.id.imgAvatar);

        // Mood Garden view + placeholder
        moodGardenView = findViewById(R.id.moodGardenView);
        imgEmptyMoodPlaceholder = findViewById(R.id.imgEmptyMoodPlaceholder);

        // ViewModel
        gratitudeViewModel = new ViewModelProvider(this).get(GratitudeViewModel.class);

        // Gratitude girdilerini gözlemle
        gratitudeViewModel.getAllEntries().observe(this, entries -> {

            if (entries != null && !entries.isEmpty()) {
                // Boş placeholder gizle, bahçeyi göster
                imgEmptyMoodPlaceholder.setVisibility(android.view.View.GONE);
                moodGardenView.setVisibility(android.view.View.VISIBLE);

                // Streak hesapla
                int currentStreak = calculateStreak(entries);
                txtStreakCount.setText(currentStreak + " Days");

                // En yeni girdiler başta olacak şekilde listeyi ters çevir
                List<GratitudeEntry> recentEntries = new ArrayList<>(entries);
                Collections.reverse(recentEntries);

                // En fazla 7 çiçek
                int maxFlowers = Math.min(recentEntries.size(), 7);
                List<GratitudeEntry> subset = recentEntries.subList(0, maxFlowers);

                // Bahçeye ver
                moodGardenView.setEntries(subset);

            } else {
                // Hiç entry yoksa
                txtStreakCount.setText("0 Days");
                moodGardenView.setVisibility(android.view.View.GONE);
                imgEmptyMoodPlaceholder.setVisibility(android.view.View.VISIBLE);
                moodGardenView.setEntries(null);
            }
        });

        // Tiny Tasks kurulumu
        setupTinyTasks();

        // FAB
        fabMain.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddGratitudeActivity.class)));

        // Bottom Nav
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ideas) {
                startActivity(new Intent(getApplicationContext(), IdeasActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileImage();
    }

    // Profil foto
    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String photoUriString = prefs.getString("userPhoto", "");

        if (!photoUriString.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(photoUriString))
                    .circleCrop()
                    .into(imgAvatar);
            imgAvatar.setPadding(0, 0, 0, 0);
        } else {
            imgAvatar.setImageResource(R.mipmap.ic_launcher);
        }
    }

    // Tiny Tasks
    private void setupTinyTasks() {
        rvTasks = findViewById(R.id.rvTasks);
        etNewTask = findViewById(R.id.etNewTask);
        btnAddNewTask = findViewById(R.id.btnAddNewTask);

        if (rvTasks != null) {
            rvTasks.setLayoutManager(new LinearLayoutManager(this));

            taskAdapter = new TaskAdapter(new TaskAdapter.OnTaskActionListener() {
                @Override
                public void onTaskCheckChanged(TaskEntry task, boolean isChecked) {
                    task.setCompleted(isChecked);
                    gratitudeViewModel.updateTask(task);
                }

                @Override
                public void onTaskDelete(TaskEntry task) {
                    gratitudeViewModel.deleteTask(task);
                }
            });
            rvTasks.setAdapter(taskAdapter);

            gratitudeViewModel.getAllTasks().observe(this, tasks -> {
                taskAdapter.setTasks(tasks);
            });

            btnAddNewTask.setOnClickListener(v -> {
                String title = etNewTask.getText().toString().trim();
                if (!title.isEmpty()) {
                    TaskEntry newTask = new TaskEntry(title, false);
                    gratitudeViewModel.insertTask(newTask);
                    etNewTask.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Write a task first!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Bildirim
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Reminder";
            String description = "Reminds you to log gratitude";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel =
                    new NotificationChannel("tiny_thanks_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void scheduleDailyNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int hour = prefs.getInt("reminderHour", 20);
        int minute = prefs.getInt("reminderMinute", 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    // Streak
    private int calculateStreak(List<GratitudeEntry> entries) {
        if (entries.isEmpty()) return 0;
        Set<String> uniqueDates = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        for (GratitudeEntry entry : entries) {
            uniqueDates.add(sdf.format(new Date(entry.getTimestamp())));
        }

        int streak = 0;
        Calendar calendar = Calendar.getInstance();
        String todayStr = sdf.format(calendar.getTime());

        if (uniqueDates.contains(todayStr)) {
            streak++;
        }

        while (true) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            String checkDate = sdf.format(calendar.getTime());
            if (uniqueDates.contains(checkDate)) {
                streak++;
            } else {
                if (streak == 0 && uniqueDates.contains(checkDate)) {
                    streak++;
                    continue;
                }
                break;
            }
        }
        return streak;
    }
}
