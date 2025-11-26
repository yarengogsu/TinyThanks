package com.example.tinythanks;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DAY_START = "extra_day_start";

    private GratitudeViewModel viewModel;
    private GratitudeAdapter adapter;
    private long dayStartMillis;
    private long dayEndMillis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);

        dayStartMillis = getIntent().getLongExtra(EXTRA_DAY_START, 0L);
        dayEndMillis = dayStartMillis + 24L * 60L * 60L * 1000L;

        TextView title = findViewById(R.id.text_day_title);
        RecyclerView recyclerView = findViewById(R.id.recycler_day_entries);

        adapter = new GratitudeAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Başlık: 22 Nov 2025 gibi
        Date date = new Date(dayStartMillis);
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        title.setText(df.format(date));

        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(GratitudeViewModel.class);

        viewModel.getAllEntries().observe(this, entries -> {
            if (entries == null) entries = new ArrayList<>();

            List<GratitudeEntry> dayEntries = new ArrayList<>();
            for (GratitudeEntry e : entries) {
                long t = e.getTimestamp();
                if (t >= dayStartMillis && t < dayEndMillis) {
                    dayEntries.add(e);
                }
            }
            adapter.setEntries(dayEntries);
        });
    }
}
