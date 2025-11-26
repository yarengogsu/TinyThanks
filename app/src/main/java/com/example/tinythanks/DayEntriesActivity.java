package com.example.tinythanks;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DayEntriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_entries);

        // RecyclerView bul
        RecyclerView rv = findViewById(R.id.recyclerview_day_entries);

        // 2 sütunlu grid layout
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        // Intent’ten gelen gün entry’leri
        List<GratitudeEntry> entries =
                (List<GratitudeEntry>) getIntent().getSerializableExtra("entries");

        if (entries == null) {
            entries = new ArrayList<>();
        }

        // Adapter bağla
        GratitudeAdapter adapter = new GratitudeAdapter(this);
        rv.setAdapter(adapter);
        adapter.setEntries(entries);
    }
}
