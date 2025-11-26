package com.example.tinythanks;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddGratitudeActivity extends AppCompatActivity {

    private EditText etContent;
    private MaterialButton btnSave;
    private View layoutAddPhoto;
    private ImageView ivSelectedPhoto;

    private Uri photoUri;
    private String currentPhotoPath;

    private GratitudeViewModel gratitudeViewModel;

    // Başlatıcılar (Launchers)
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> selectPictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gratitude);

        // 1. Veritabanı Bağlantısı
        gratitudeViewModel = new ViewModelProvider(this).get(GratitudeViewModel.class);

        // 2. Bileşenleri Bağla
        etContent = findViewById(R.id.etGratitudeContent);
        btnSave = findViewById(R.id.btnSave);
        layoutAddPhoto = findViewById(R.id.layoutAddPhoto);
        ivSelectedPhoto = findViewById(R.id.ivSelectedPhoto);

        // 3. Diğer sayfadan gelen "Fikir/Soru" varsa kutuya yaz
        if (getIntent().hasExtra("PROMPT_TEXT")) {
            String prompt = getIntent().getStringExtra("PROMPT_TEXT");
            etContent.setText(prompt + "\n\n");
            etContent.setSelection(etContent.getText().length()); // İmleci sona koy
        }

        // 4. Tıklama Olayları
        layoutAddPhoto.setOnClickListener(v -> showImageSourceDialog());
        btnSave.setOnClickListener(v -> saveGratitude());

        // --- KAMERA SONUCU ---
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                showImagePreview(photoUri);
            }
        });

        // --- GALERİ SONUCU ---
        selectPictureLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                photoUri = uri;
                currentPhotoPath = uri.toString();
                showImagePreview(uri);
            }
        });

        // --- İZİN SONUCU (Kamera İçin) ---
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                dispatchTakePictureIntent(); // İzin verildiyse kamerayı aç
            } else {
                Toast.makeText(this, "Camera permission required!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- FOTOĞRAF KAYNAĞI SEÇİMİ ---
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Kamera seçilince önce izni kontrol et!
                checkCameraPermissionAndOpen();
            } else {
                selectPictureLauncher.launch("image/*"); // Galeri direkt açılır
            }
        });
        builder.show();
    }

    // --- GÜVENLİ İZİN KONTROLÜ ---
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent(); // İzin zaten var
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA); // İzin iste
        }
    }

    // --- KAMERAYI AÇMA VE DOSYA OLUŞTURMA ---
    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            // KRİTİK NOKTA: Paket ismini otomatik alarak hata yapmayı önlüyoruz
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
        currentPhotoPath = image.getAbsolutePath(); // Dosya yolunu kaydet
        return image;
    }

    private void showImagePreview(Uri uri) {
        ivSelectedPhoto.setVisibility(View.VISIBLE);
        ivSelectedPhoto.setImageURI(uri);
        // İkonları gizle ki resim görünsün
        findViewById(R.id.imgCameraIcon).setVisibility(View.GONE);
        findViewById(R.id.tvAddPhoto).setVisibility(View.GONE);
    }

    // --- VERİTABANINA KAYDETME ---
    private void saveGratitude() {
        String text = etContent.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Please write something! 🙏", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        // Kaydı oluştur
        GratitudeEntry newEntry = new GratitudeEntry(text, currentPhotoPath, timestamp);

        // Veritabanına ekle
        gratitudeViewModel.insert(newEntry);

        Toast.makeText(this, "Saved! 🎉", Toast.LENGTH_SHORT).show();
        finish(); // Ana ekrana dön
    }
}