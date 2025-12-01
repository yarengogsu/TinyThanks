package com.example.tinythanks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide; // Glide kütüphanesi ekliyoruz (Büyük resimler için şart)

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EntryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_TEXT = "extra_text";
    public static final String EXTRA_PHOTO_PATH = "extra_photo_path";
    public static final String EXTRA_TIMESTAMP = "extra_timestamp";
    public static final String EXTRA_MOOD = "extra_mood"; // YENİ: Mood verisi için anahtar

    private ImageView photoView;
    private TextView dateText;
    private EditText contentEdit;
    private Button saveButton;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    private int entryId = -1;
    private String photoPath;
    private long timestamp;
    private int currentMood; // YENİ: Mevcut modu saklamak için

    private GratitudeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);

        photoView = findViewById(R.id.image_detail_photo);
        dateText = findViewById(R.id.text_detail_date);
        contentEdit = findViewById(R.id.text_detail_content);
        saveButton = findViewById(R.id.btn_save_changes);

        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(GratitudeViewModel.class);

        // --- Verileri Al ---
        String text = getIntent().getStringExtra(EXTRA_TEXT);
        photoPath = getIntent().getStringExtra(EXTRA_PHOTO_PATH);
        timestamp = getIntent().getLongExtra(EXTRA_TIMESTAMP, -1);
        entryId = getIntent().getIntExtra(EXTRA_ID, -1);
        currentMood = getIntent().getIntExtra(EXTRA_MOOD, 3); // Varsayılan 3 (İyi)

        // Yazıyı koy
        if (!TextUtils.isEmpty(text)) {
            contentEdit.setText(text);
        }

        // Tarihi koy
        if (timestamp > 0) {
            Date date = new Date(timestamp);
            dateText.setText(dateFormat.format(date));
        } else {
            dateText.setText("");
        }

        // Resmi koy (Glide ile daha güvenli)
        if (photoPath != null && !photoPath.isEmpty()) {
            Glide.with(this)
                    .load(new File(photoPath))
                    .into(photoView);
        } else {
            photoView.setImageResource(R.drawable.img_morning); // Varsayılan
        }

        // --- DEĞİŞİKLİKLERİ KAYDET ---
        saveButton.setOnClickListener(v -> {
            String newText = contentEdit.getText().toString().trim();

            if (entryId == -1) {
                Toast.makeText(this, "Hata: ID bulunamadı", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(newText)) {
                Toast.makeText(this, "Lütfen bir şeyler yazın", Toast.LENGTH_SHORT).show();
                return;
            }

            // GÜNCELLEME NESNESİ OLUŞTURMA (Düzeltilen Kısım)
            // Artık 'currentMood' değerini de veriyoruz ki eski mod silinmesin.
            GratitudeEntry updated = new GratitudeEntry(newText, photoPath, timestamp, currentMood);
            updated.setId(entryId);

            viewModel.update(updated);

            Toast.makeText(this, "Güncellendi! ✅", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}