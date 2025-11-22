package com.example.tinythanks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Shows a single gratitude entry in detail:
 * - big photo (if any)
 * - editable text
 * - date / time
 * - "Save changes" button that updates Room
 */
public class EntryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_TEXT = "extra_text";
    public static final String EXTRA_PHOTO_PATH = "extra_photo_path";
    public static final String EXTRA_TIMESTAMP = "extra_timestamp";

    private ImageView photoView;
    private TextView dateText;
    private EditText contentEdit;
    private Button saveButton;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    private int entryId = -1;
    private String photoPath;
    private long timestamp;

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

        // --- read data from Intent ---
        String text = getIntent().getStringExtra(EXTRA_TEXT);
        photoPath = getIntent().getStringExtra(EXTRA_PHOTO_PATH);
        timestamp = getIntent().getLongExtra(EXTRA_TIMESTAMP, -1);
        entryId = getIntent().getIntExtra(EXTRA_ID, -1);

        // set text
        if (!TextUtils.isEmpty(text)) {
            contentEdit.setText(text);
        }

        // set date
        if (timestamp > 0) {
            Date date = new Date(timestamp);
            dateText.setText(dateFormat.format(date));
        } else {
            dateText.setText("");
        }

        // load photo if exists
        if (!TextUtils.isEmpty(photoPath)) {
            File file = new File(photoPath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                photoView.setImageBitmap(bitmap);
            } else {
                photoView.setImageDrawable(null);
            }
        } else {
            photoView.setImageDrawable(null);
        }

        // --- save changes button ---
        saveButton.setOnClickListener(v -> {
            String newText = contentEdit.getText().toString().trim();

            if (entryId == -1) {
                Toast.makeText(this, "Cannot update: missing entry id", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(newText)) {
                Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // keep same timestamp & photoPath; only text is changed
            GratitudeEntry updated = new GratitudeEntry(newText, photoPath, timestamp);
            updated.setId(entryId);  // IMPORTANT: tell Room which row to update

            viewModel.update(updated);

            Toast.makeText(this, "Entry updated", Toast.LENGTH_SHORT).show();
            finish(); // go back to previous screen
        });
    }
}
