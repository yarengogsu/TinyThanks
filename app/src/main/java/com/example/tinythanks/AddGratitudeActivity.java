package com.example.tinythanks;

import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;


import androidx.lifecycle.ViewModelProvider;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;




public class AddGratitudeActivity extends AppCompatActivity {
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ImageView photoPreview;
    private EditText editTextGratitude;
    private Button buttonSave;
    private Button buttonAddPhoto;
    private Uri photoUri;
    private GratitudeViewModel gratitudeViewModel;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> selectPictureLauncher;
    private String currentPhotoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((R.layout.activity_add_gratitude));

        editTextGratitude = findViewById(R.id.edit_text_gratitude);
        buttonSave = findViewById((R.id.button_save_gratitude));
        buttonAddPhoto = findViewById((R.id.button_add_photo));
        photoPreview = findViewById(R.id.image_view_photo_preview);

        gratitudeViewModel = (GratitudeViewModel) new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(GratitudeViewModel.class);
        buttonSave.setOnClickListener(v -> saveGratitude());
        buttonAddPhoto.setOnClickListener(v -> showImageSourceDialog());

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                    if (isGranted.get(android.Manifest.permission.CAMERA) == Boolean.TRUE) {
                        dispatchTakePictureIntent(); // İzin varsa kamerayı aç
                    } else if (isGranted.get(android.Manifest.permission.READ_MEDIA_IMAGES) == Boolean.TRUE) {
                        selectPictureLauncher.launch("image/*"); // İzin varsa galeriyi aç
                    } else {
                        Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show();
                    }
                });


        // 2. Fotoğraf Çekme Launcher'ı
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                // Başarılı olursa fotoğrafı gösterme metodu çağrılacak
                showImagePreview(photoUri); // photoUri'yi kullanıyoruz
            } else {
                Toast.makeText(this, "Photo capture cancelled.", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Galeriden Seçme Launcher'ı
        selectPictureLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                // Seçilen URI'yi Activity'de saklama ve önizleme gösterme
                photoUri = uri;
                showImagePreview(photoUri);
            }
        });
    };
    private void requestPermissionsIfNecessary(boolean isCamera) {
        String[] permissions;
        if (isCamera) {
            // Kamera ve depolama izni (Android 13+)
            permissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            // Sadece depolama izni
            permissions = new String[]{android.Manifest.permission.READ_MEDIA_IMAGES};
        }

        // Launcher'ı kullanarak izinleri iste
        requestPermissionLauncher.launch(permissions);
    }

    private void saveGratitude() {
        String gratitudeText = editTextGratitude.getText().toString().trim();

        if (gratitudeText.isEmpty()) {
            Toast.makeText(this, "Please enter a gratitude text.", Toast.LENGTH_SHORT).show();
            return;
        }
        long timestamp = new Date().getTime();
        GratitudeEntry newEntry = new GratitudeEntry(gratitudeText, currentPhotoPath, timestamp);
        gratitudeViewModel.insert(newEntry);
        Toast.makeText(this, "Gratitude saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }


    private void showImageSourceDialog() {
        String[] options = {"Take Photo with Camera", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // Take Photo
                dispatchTakePictureIntent();
            } else if (which == 1) { // Choose from Gallery
                selectPictureLauncher.launch("image/*");
            }
        });
        builder.show();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "File creation error.", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(this,
                    "com.example.tinythanks.fileprovider",
                    photoFile);

            takePictureLauncher.launch(photoUri);
        }
    }

    private void showImagePreview(Uri uri) {
        currentPhotoPath = uri.toString();
-
        ImageView photoPreview = findViewById(R.id.image_view_photo_preview);
        photoPreview.setImageURI(uri);
    }
}