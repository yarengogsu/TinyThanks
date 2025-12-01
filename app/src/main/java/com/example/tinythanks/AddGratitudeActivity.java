package com.example.tinythanks;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddGratitudeActivity extends AppCompatActivity {

    private EditText etContent;
    private MaterialButton btnSave;
    private View layoutAddPhoto;
    private ImageView ivSelectedPhoto;
    private TextView tvTitle;

    private Uri photoUri;
    private String currentPhotoPath;

    private GratitudeViewModel gratitudeViewModel;

    private boolean isEditMode = false;
    private int existingId = -1;
    private long existingTimestamp;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> selectPictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    // --- 5'Lİ MOOD DEĞİŞKENLERİ ---
    private ImageView btnMoodPeaceful, btnMoodJoyful, btnMoodEnergetic, btnMoodCreative, btnMoodLoved;
    // Varsayılan: Neşeli (Joyful - 2.0) veya Enerjik (3.0) seçebilirsin.
    // Burada ortadaki "Enerjik" (Mavi) ile başlıyoruz.
    private double selectedMood = 3.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gratitude);

        gratitudeViewModel = new ViewModelProvider(this).get(GratitudeViewModel.class);

        // Görünümleri Bağla
        etContent = findViewById(R.id.etGratitudeContent);
        btnSave = findViewById(R.id.btnSave);
        layoutAddPhoto = findViewById(R.id.layoutAddPhoto);
        ivSelectedPhoto = findViewById(R.id.ivSelectedPhoto);
        tvTitle = findViewById(R.id.tvTitle);

        // --- BUTONLARI BAĞLA ---
        btnMoodPeaceful = findViewById(R.id.btnMoodPeaceful);   // 1. Yeşil
        btnMoodJoyful = findViewById(R.id.btnMoodJoyful);       // 2. Sarı
        btnMoodEnergetic = findViewById(R.id.btnMoodEnergetic); // 3. Mavi
        btnMoodCreative = findViewById(R.id.btnMoodCreative);   // 4. Mor
        btnMoodLoved = findViewById(R.id.btnMoodLoved);         // 5. Pembe

        // --- TIKLAMA OLAYLARI (1-5 Arası Puanlama) ---

        // 1. Huzurlu (Yeşil)
        btnMoodPeaceful.setOnClickListener(v -> {
            selectedMood = 1.0;
            updateMoodSelection();
        });

        // 2. Neşeli (Sarı)
        btnMoodJoyful.setOnClickListener(v -> {
            selectedMood = 2.0;
            updateMoodSelection();
        });

        // 3. Enerjik (Mavi)
        btnMoodEnergetic.setOnClickListener(v -> {
            selectedMood = 3.0;
            updateMoodSelection();
        });

        // 4. Yaratıcı (Mor)
        btnMoodCreative.setOnClickListener(v -> {
            selectedMood = 4.0;
            updateMoodSelection();
        });

        // 5. Sevgi Dolu (Pembe)
        btnMoodLoved.setOnClickListener(v -> {
            selectedMood = 5.0;
            updateMoodSelection();
        });

        // Başlangıçta UI'ı güncelle
        updateMoodSelection();

        // --- DÜZENLEME MODU KONTROLÜ ---
        Intent intent = getIntent();
        if (intent.hasExtra("EXTRA_ID")) {
            isEditMode = true;
            existingId = intent.getIntExtra("EXTRA_ID", -1);
            existingTimestamp = intent.getLongExtra("EXTRA_TIMESTAMP", System.currentTimeMillis());

            etContent.setText(intent.getStringExtra("EXTRA_CONTENT"));
            currentPhotoPath = intent.getStringExtra("EXTRA_IMAGE_PATH");

            // Kayıtlı Mood'u Geri Yükle
            double mood = intent.getDoubleExtra("EXTRA_MOOD", 3.0);
            if (mood == 0) mood = intent.getIntExtra("EXTRA_MOOD", 3);

            selectedMood = mood;
            updateMoodSelection();

            if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
                showImagePreview(currentPhotoPath);
            }

            tvTitle.setText("Edit Entry");
            btnSave.setText("Update Entry");
        } else if (intent.hasExtra("PROMPT_TEXT")) {
            String prompt = intent.getStringExtra("PROMPT_TEXT");
            etContent.setText(prompt + "\n\n");
            etContent.setSelection(etContent.getText().length());
        }

        // Diğer Tıklamalar
        layoutAddPhoto.setOnClickListener(v -> showImageSourceDialog());
        btnSave.setOnClickListener(v -> saveOrUpdateGratitude());

        // --- KAMERA/GALERİ SONUÇLARI ---
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) showImagePreview(currentPhotoPath);
        });

        selectPictureLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String newPath = copyFileFromUri(uri);
                if (newPath != null) {
                    currentPhotoPath = newPath;
                    showImagePreview(currentPhotoPath);
                } else Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        });

        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) dispatchTakePictureIntent();
            else Toast.makeText(this, "Camera permission required!", Toast.LENGTH_SHORT).show();
        });
    }

    // --- RENKLİ SEÇİM MANTIĞI (5 RENK) ---
    private void updateMoodSelection() {
        // 1. Hepsini önce gri ve normal boyut yap
        int grayColor = ContextCompat.getColor(this, R.color.psych_gray);

        resetButtonStyle(btnMoodPeaceful, grayColor);
        resetButtonStyle(btnMoodJoyful, grayColor);
        resetButtonStyle(btnMoodEnergetic, grayColor);
        resetButtonStyle(btnMoodCreative, grayColor);
        resetButtonStyle(btnMoodLoved, grayColor);

        // 2. Seçili olanı kendi rengine boya ve büyüt
        // Not: Puanlar 1.0 ile 5.0 arasındadır
        if (selectedMood == 1.0) {
            highlightButtonStyle(btnMoodPeaceful, R.color.psych_peaceful); // Yeşil
        } else if (selectedMood == 2.0) {
            highlightButtonStyle(btnMoodJoyful, R.color.psych_joyful);     // Sarı
        } else if (selectedMood == 3.0) {
            highlightButtonStyle(btnMoodEnergetic, R.color.psych_energetic); // Mavi
        } else if (selectedMood == 4.0) {
            highlightButtonStyle(btnMoodCreative, R.color.psych_creative);   // Mor
        } else {
            highlightButtonStyle(btnMoodLoved, R.color.psych_loved);         // Pembe
        }
    }

    private void resetButtonStyle(ImageView view, int color) {
        view.setColorFilter(color);
        // Küçültüp normale döndür
        view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
    }

    private void highlightButtonStyle(ImageView view, int colorResId) {
        view.setColorFilter(ContextCompat.getColor(this, colorResId));
        // Seçilince hafif büyüt (Zoom efekti)
        view.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();
    }

    // --- DOSYA VE KAYIT İŞLEMLERİ (Standart) ---
    private String copyFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "GAL_" + timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File newFile = new File(storageDir, imageFileName);
            OutputStream outputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
            outputStream.close(); inputStream.close();
            return newFile.getAbsolutePath();
        } catch (IOException e) { e.printStackTrace(); return null; }
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) checkCameraPermissionAndOpen();
            else selectPictureLauncher.launch("image/*");
        });
        builder.show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) dispatchTakePictureIntent();
        else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try { photoFile = createImageFile(); } catch (IOException ex) { Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show(); }
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
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void showImagePreview(String path) {
        ivSelectedPhoto.setVisibility(View.VISIBLE);
        Glide.with(this).load(path).centerCrop().into(ivSelectedPhoto);
        findViewById(R.id.imgCameraIcon).setVisibility(View.GONE);
        findViewById(R.id.tvAddPhoto).setVisibility(View.GONE);
    }

    private void saveOrUpdateGratitude() {
        String text = etContent.getText().toString().trim();
        if (text.isEmpty()) { Toast.makeText(this, "Please write something!", Toast.LENGTH_SHORT).show(); return; }

        if (isEditMode) {
            // Güncelleme yaparken de yeni mood'u (selectedMood) gönderiyoruz
            GratitudeEntry updatedEntry = new GratitudeEntry(existingId, text, currentPhotoPath, existingTimestamp, (int) selectedMood);
            gratitudeViewModel.update(updatedEntry);
            Toast.makeText(this, "Updated successfully! ✅", Toast.LENGTH_SHORT).show();
        } else {
            // Yeni kayıtta da
            long timestamp = System.currentTimeMillis();
            GratitudeEntry newEntry = new GratitudeEntry(text, currentPhotoPath, timestamp, (int) selectedMood);
            gratitudeViewModel.insert(newEntry);
            Toast.makeText(this, "Saved! 🎉", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}