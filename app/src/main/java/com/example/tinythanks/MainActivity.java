package com.example.tinythanks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private GratitudeViewModel gratitudeViewModel;
    private TextView emptyView;

    private GratitudeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. FAB button ---
        FloatingActionButton fab = findViewById(R.id.fab_add_entry);

        // Short click → AddGratitudeActivity (NEW ENTRY)
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddGratitudeActivity.class);
            startActivity(intent);
        });

        // Long press → FlowerGardenActivity (flower garden)
        fab.setOnLongClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FlowerGardenActivity.class);
            startActivity(intent);
            return true;
        });


        // --- 2. RecyclerView & empty view ---
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        emptyView = findViewById(R.id.empty_view);

        adapter = new GratitudeAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- 3. ViewModel ---
        gratitudeViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(GratitudeViewModel.class);

        // --- 4. LiveData observe ---
        gratitudeViewModel.getAllEntries().observe(this, entries -> {
            adapter.setEntries(entries);

            if (entries == null || entries.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}
