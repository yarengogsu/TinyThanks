package com.example.tinythanks;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide; // Glide library added
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProfileActivity extends AppCompatActivity {

    private GratitudeViewModel gratitudeViewModel;
    private ShapeableImageView imgProfile;
    private TextView tvProfileTitle;
    private TextView tvReminderTime;
    private TextView tvTotalCount;
    private TextView tvLongestStreak;

    private Uri tempSelectedUri;
    private Uri photoUri;
    private ImageView dialogProfilePreview;

    // Launchers
    private ActivityResultLauncher<String> selectPhotoLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // --- BIND VIEWS ---
        imgProfile = findViewById(R.id.imgProfile);
        tvProfileTitle = findViewById(R.id.tvProfileTitle);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvLongestStreak = findViewById(R.id.tvLongestStreak);
        MaterialButton btnSetTime = findViewById(R.id.btnSetTime);

        TextView tvEditProfile = findViewById(R.id.tvEditProfile);
        tvEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // --- LOAD DATA ---
        loadProfileData();
        loadReminderTime();

        // --- SET REMINDER TIME ---
        btnSetTime.setOnClickListener(v -> showTimePicker());

        // --- 1. GALLERY RESULT (FIXED WITH GLIDE) ---
        selectPhotoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                tempSelectedUri = uri;

                // Try to persist permission for gallery URI
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                if (dialogProfilePreview != null) {
                    // Load with Glide
                    Glide.with(this)
                            .load(uri)
                            .circleCrop()
                            .into(dialogProfilePreview);
                }
            }
        });

        // --- 2. CAMERA RESULT ---
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                tempSelectedUri = photoUri;
                if (dialogProfilePreview != null) {
                    // Load with Glide
                    Glide.with(this)
                            .load(photoUri)
                            .circleCrop()
                            .into(dialogProfilePreview);
                }
            }
        });

        // --- 3. PERMISSION RESULT ---
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission required!", Toast.LENGTH_SHORT).show();
            }
        });

        imgProfile.setOnClickListener(v -> showEditProfileDialog());

        // --- UPDATE STATISTICS ---
        gratitudeViewModel = new ViewModelProvider(this).get(GratitudeViewModel.class);

        // CHANGED: getAllEntries -> getAllGratitudes
        gratitudeViewModel.getAllEntries().observe(this, entries -> {
            if (entries != null) {
                // 1. Total Count
                tvTotalCount.setText(String.valueOf(entries.size()));

                // 2. Longest Streak
                int longest = calculateLongestStreak(entries);
                tvLongestStreak.setText(longest + " Days");
            } else {
                tvTotalCount.setText("0");
                tvLongestStreak.setText("0 Days");
            }
        });

        setupNavigation();
    }

    // --- CALCULATE LONGEST STREAK ---
    private int calculateLongestStreak(List<GratitudeEntry> entries) {
        if (entries.isEmpty()) return 0;

        Set<String> uniqueDates = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        for (GratitudeEntry entry : entries) {
            uniqueDates.add(sdf.format(new Date(entry.getTimestamp())));
        }

        List<String> sortedDates = new ArrayList<>(uniqueDates);
        Collections.sort(sortedDates);

        int maxStreak = 0;
        int currentStreak = 0;
        long previousTime = 0;

        for (String dateStr : sortedDates) {
            try {
                long currentTime = sdf.parse(dateStr).getTime();

                if (previousTime == 0) {
                    currentStreak = 1;
                } else {
                    long diff = currentTime - previousTime;
                    long daysDiff = diff / (1000 * 60 * 60 * 24);

                    if (daysDiff == 1) {
                        currentStreak++;
                    } else {
                        if (currentStreak > maxStreak) maxStreak = currentStreak;
                        currentStreak = 1;
                    }
                }
                if (currentStreak > maxStreak) maxStreak = currentStreak;
                previousTime = currentTime;

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return maxStreak;
    }

    // --- TIME PICKER ---
    private void showTimePicker() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int hour = prefs.getInt("reminderHour", 20);
        int minute = prefs.getInt("reminderMinute", 0);

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Select Reminder Time")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int newHour = picker.getHour();
            int newMinute = picker.getMinute();

            saveReminderTime(newHour, newMinute);
            scheduleNotification(newHour, newMinute);

            Toast.makeText(this, "Reminder updated", Toast.LENGTH_SHORT).show();
        });

        picker.show(getSupportFragmentManager(), "tag");
    }

    private void saveReminderTime(int hour, int minute) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("reminderHour", hour);
        editor.putInt("reminderMinute", minute);
        editor.apply();

        tvReminderTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
    }

    private void loadReminderTime() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int hour = prefs.getInt("reminderHour", 20);
        int minute = prefs.getInt("reminderMinute", 0);
        tvReminderTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
    }

    private void scheduleNotification(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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

    // --- EDIT PROFILE DIALOG ---
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etEditName);
        dialogProfilePreview = dialogView.findViewById(R.id.imgEditProfilePreview);
        MaterialButton btnChangePhoto = dialogView.findViewById(R.id.btnChangePhoto);
        MaterialButton btnSaveProfile = dialogView.findViewById(R.id.btnSaveProfile);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentName = prefs.getString("userName", "Profile");
        String currentPhoto = prefs.getString("userPhoto", "");

        etName.setText(currentName.equals("Profile") ? "" : currentName);

        if (!currentPhoto.isEmpty()) {
            // Load with Glide
            Glide.with(this)
                    .load(Uri.parse(currentPhoto))
                    .circleCrop()
                    .placeholder(R.drawable.img_morning)
                    .into(dialogProfilePreview);
        }

        btnChangePhoto.setOnClickListener(v -> showImageSourceDialog());

        btnSaveProfile.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            if (newName.isEmpty()) newName = "Profile";

            saveProfileData(newName, tempSelectedUri != null ? tempSelectedUri.toString() : currentPhoto);
            dialog.dismiss();
        });

        dialog.show();
    }

    // --- IMAGE SOURCE DIALOG (CLEAN - NO EMOJIS) ---
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkCameraPermissionAndOpen();
            } else {
                selectPhotoLauncher.launch("image/*");
            }
        });
        builder.show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "File error", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            String authority = getPackageName() + ".fileprovider";
            photoUri = FileProvider.getUriForFile(this, authority, photoFile);
            takePictureLauncher.launch(photoUri);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void saveProfileData(String name, String photoUriString) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userName", name);
        editor.putString("userPhoto", photoUriString);
        editor.apply();
        loadProfileData();
    }

    // --- LOAD PROFILE DATA (FIXED WITH GLIDE) ---
    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String name = prefs.getString("userName", "Profile");
        String photoUriString = prefs.getString("userPhoto", "");

        tvProfileTitle.setText(name);

        if (!photoUriString.isEmpty()) {
            // Load with Glide
            Glide.with(this)
                    .load(Uri.parse(photoUriString))
                    .circleCrop()
                    .placeholder(R.drawable.img_morning)
                    .error(R.drawable.img_morning)
                    .into(imgProfile);
        } else {
            imgProfile.setImageResource(R.drawable.img_morning);
        }
    }

    private void setupNavigation() {
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, AddGratitudeActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_ideas) {
                startActivity(new Intent(getApplicationContext(), IdeasActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}