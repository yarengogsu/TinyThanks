package com.example.tinythanks;

import android.Manifest;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private GratitudeViewModel gratitudeViewModel;
    private ShapeableImageView imgProfile;
    private TextView tvProfileTitle;

    private Uri tempSelectedUri;
    private Uri photoUri;
    private ImageView dialogProfilePreview;

    private ActivityResultLauncher<String> selectPhotoLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // --- BİLEŞENLERİ BAĞLAMA ---
        imgProfile = findViewById(R.id.imgProfile);
        tvProfileTitle = findViewById(R.id.tvProfileTitle);
        TextView tvTotalCount = findViewById(R.id.tvTotalCount);

        loadProfileData();

        // --- 1. GALERİ SONUCU ---
        selectPhotoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                tempSelectedUri = uri;
                if (dialogProfilePreview != null) {
                    dialogProfilePreview.setImageURI(uri);
                }
            }
        });

        // --- 2. KAMERA SONUCU ---
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                tempSelectedUri = photoUri;
                if (dialogProfilePreview != null) {
                    dialogProfilePreview.setImageURI(photoUri);
                }
            }
        });

        // --- 3. İZİN SONUCU (KAMERA İÇİN) ---
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission required to take photos!", Toast.LENGTH_SHORT).show();
            }
        });

        imgProfile.setOnClickListener(v -> showEditProfileDialog());

        // --- İSTATİSTİKLERİ GÜNCELLE ---
        gratitudeViewModel = new ViewModelProvider(this).get(GratitudeViewModel.class);
        // DÜZELTME: getAllEntries yerine getAllGratitudes kullanıyoruz (ViewModel ile uyumlu)
        gratitudeViewModel.getAllEntries().observe(this, entries -> {
            if (entries != null) {
                tvTotalCount.setText(String.valueOf(entries.size()));
            }
        });

        setupNavigation();
    }

    // --- PROFİL DÜZENLEME PENCERESİ ---
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
            try {
                dialogProfilePreview.setImageURI(Uri.parse(currentPhoto));
            } catch (Exception e) {
                dialogProfilePreview.setImageResource(R.drawable.img_morning);
            }
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

    // --- KAMERA MI GALERİ Mİ? ---
    private void showImageSourceDialog() {
        String[] options = {"Take Photo 📸", "Choose from Gallery 🖼️"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Kamera seçilince önce izni kontrol et!
                checkCameraPermissionAndOpen();
            } else {
                selectPhotoLauncher.launch("image/*");
            }
        });
        builder.show();
    }

    // --- GÜVENLİ KAMERA AÇMA (ÇÖKMEYİ ÖNLER) ---
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
            Toast.makeText(this, "Dosya oluşturulamadı", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(this,
                    "com.example.tinythanks.fileprovider", // BURASI MANIFEST İLE AYNI MI?
                    photoFile);

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

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String name = prefs.getString("userName", "Profile");
        String photoUriString = prefs.getString("userPhoto", "");

        tvProfileTitle.setText(name);
        if (!photoUriString.isEmpty()) {
            try {
                imgProfile.setImageURI(Uri.parse(photoUriString));
            } catch (Exception e) {
                imgProfile.setImageResource(R.drawable.img_morning);
            }
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