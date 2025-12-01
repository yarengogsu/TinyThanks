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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    // Tiny Tasks Değişkenleri
    private EditText etNewTask;
    private ImageView btnAddNewTask;
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. BİLDİRİM SİSTEMİNİ HAZIRLA ---
        createNotificationChannel();
        askNotificationPermission();
        scheduleDailyNotification();

        // --- 2. BİLEŞENLERİ BAĞLAMA ---
        FloatingActionButton fabMain = findViewById(R.id.fab_add);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        txtStreakCount = findViewById(R.id.txt_streak_count);
        imgAvatar = findViewById(R.id.imgAvatar);

        // --- 3. VIEWMODEL VE ÇİÇEK MANTIĞI ---
        gratitudeViewModel = new ViewModelProvider(this).get(GratitudeViewModel.class);

        gratitudeViewModel.getAllEntries().observe(this, entries -> {
            // XML'deki bileşenleri bul
            RelativeLayout flowerContainer = findViewById(R.id.flowerContainer);
            ImageView imgEmptyPlaceholder = findViewById(R.id.imgEmptyMoodPlaceholder);
            TextView tvMoodTitle = findViewById(R.id.tvMoodTitle); // Başlık

            if (entries != null && !entries.isEmpty()) {
                // Veri var: Boş ikonunu gizle, çiçek kutusunu göster
                imgEmptyPlaceholder.setVisibility(android.view.View.GONE);
                flowerContainer.setVisibility(android.view.View.VISIBLE);

                // A. Streak Hesapla
                int currentStreak = calculateStreak(entries);
                txtStreakCount.setText(currentStreak + " Days");

                // B. Çiçek Oluşturma Mantığı
                flowerContainer.removeAllViews(); // Önce eski çiçekleri temizle

                // Son 8 girdiyi al (Çok fazla yaprak üst üste binmesin diye)
                List<GratitudeEntry> recentEntries = new ArrayList<>(entries);
                Collections.reverse(recentEntries); // En yeniler başa gelsin
                int petalCount = Math.min(recentEntries.size(), 8);

                // Matematik: 360 dereceyi yaprak sayısına böl
                float angleStep = 360f / petalCount;

                // YARIÇAP AYARI:
                // Ekran yoğunluğuna göre 35dp mesafe.
                float density = getResources().getDisplayMetrics().density;
                int radius = (int) (35 * density);

                for (int i = 0; i < petalCount; i++) {
                    GratitudeEntry entry = recentEntries.get(i);

                    // 1. Yaprağı Oluştur
                    ImageView petal = new ImageView(this);
                    // Keskin yaprak şeklini kullanıyoruz
                    petal.setImageResource(R.drawable.ic_flower_petal_sharp);

                    // 2. Yaprağı Merkeze Koy (Başlangıç noktası olarak)
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    petal.setLayoutParams(params);

                    // --- 3. RENK BELİRLEME (YENİ 5'Lİ SİSTEM) ---
                    // Artık 1, 2, 3, 4, 5 değerlerini kontrol ediyoruz.
                    int color;
                    int moodVal = (int) entry.getMood();

                    if (moodVal == 1) {
                        color = ContextCompat.getColor(this, R.color.psych_peaceful);   // Yeşil (Huzurlu)
                    } else if (moodVal == 2) {
                        color = ContextCompat.getColor(this, R.color.psych_joyful);     // Sarı (Neşeli)
                    } else if (moodVal == 3) {
                        color = ContextCompat.getColor(this, R.color.psych_energetic);  // Mavi (Enerjik)
                    } else if (moodVal == 4) {
                        color = ContextCompat.getColor(this, R.color.psych_creative);   // Mor (Yaratıcı)
                    } else {
                        color = ContextCompat.getColor(this, R.color.psych_loved);      // Pembe (Sevgi)
                    }

                    // Rengi Uygula
                    petal.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

                    // 4. Döndür ve Yerleştir (Matematiksel Düzeltme)
                    float angle = i * angleStep;
                    double rad = Math.toRadians(angle);

                    // A) Yaprağın ucunu dışarı baktır
                    petal.setRotation(angle);

                    // B) Yaprağın konumunu hesapla
                    float xPos = (float) (radius * Math.sin(rad));
                    float yPos = (float) (-radius * Math.cos(rad));

                    petal.setTranslationX(xPos);
                    petal.setTranslationY(yPos);

                    // 5. Kutuyu Ekle
                    flowerContainer.addView(petal);
                }

                // C. Çiçeğin Ortasındaki Beyaz Daireyi Ekle
                ImageView centerCircle = new ImageView(this);
                centerCircle.setImageResource(R.drawable.ic_flower_center);
                RelativeLayout.LayoutParams centerParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                centerParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                centerCircle.setLayoutParams(centerParams);
                flowerContainer.addView(centerCircle);

            } else {
                // Veri yoksa
                txtStreakCount.setText("0 Days");
                flowerContainer.setVisibility(android.view.View.GONE);
                imgEmptyPlaceholder.setVisibility(android.view.View.VISIBLE);
            }
        });

        // --- 4. TINY TASKS ---
        setupTinyTasks();

        // --- 5. TIKLAMA OLAYLARI ---
        fabMain.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AddGratitudeActivity.class));
        });

        // --- 6. ALT MENÜ GEÇİŞLERİ ---
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

    // --- PROFİL FOTOSUNU YÜKLEME ---
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

    // --- TINY TASKS KURULUMU ---
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

    // --- BİLDİRİM VE ALARM ---
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Reminder";
            String description = "Reminds you to log gratitude";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("tiny_thanks_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void scheduleDailyNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    // --- STREAK MANTIĞI ---
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