package com.example.tinythanks;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Bildirim Kanalını Kur
        createNotificationChannel();

        // 2. Bildirim İzni İste (Android 13+)
        askNotificationPermission();

        // 3. Günlük Hatırlatıcıyı Kur (Saat 20:00)
        scheduleDailyNotification();

        // --- MEVCUT KODLARIN AYNI KALIYOR ---

        FloatingActionButton fabMain = findViewById(R.id.fab_add);
        FloatingActionButton fabQuick = findViewById(R.id.fab_quick_inner);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        fabMain.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddGratitudeActivity.class);
            startActivity(intent);
        });

        fabQuick.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddGratitudeActivity.class);
            startActivity(intent);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            }
            else if (id == R.id.nav_history) {
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (id == R.id.nav_ideas) {
                startActivity(new Intent(getApplicationContext(), IdeasActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    // --- BİLDİRİM KANALI OLUŞTURMA ---
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "TinyThanksReminder";
            String description = "Günlük şükür hatırlatıcısı";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("tiny_thanks_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // --- İZİN İSTEME (Android 13+) ---
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // --- ALARM KURMA (Her Gün 20:00) ---
    private void scheduleDailyNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Saati ayarla
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 20); // Akşam 8 (20:00)
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Eğer saat 20:00'ı geçtiyse yarına kur
        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Alarmı tekrarla (Günde 1 kez)
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }
}