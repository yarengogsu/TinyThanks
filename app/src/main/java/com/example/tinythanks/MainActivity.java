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

    // Adapter'ı sınıf içinde tanımlıyoruz ki, observe bloğu erişebilsin
    private GratitudeAdapter adapter;
    // HATA: Önceden bu satır silindiği için hata veriyordu!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. FAB Butonunu Bağlama ---
        FloatingActionButton fab = findViewById(R.id.fab_add_entry);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddGratitudeActivity.class);
            startActivity(intent);
        });

        // --- 2. RecyclerView ve EmptyView'ı Bağlama ---
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        emptyView = findViewById(R.id.empty_view); // Yeni XML ID'si

        // Adapter'ı Başlatma (Sınıfın dışında tanımladığımız değişkene atama)
        adapter = new GratitudeAdapter(this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- 3. ViewModel'i Başlatma ---
        gratitudeViewModel = (GratitudeViewModel) new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(GratitudeViewModel.class);


        // --- 4. LiveData'yı Gözlemleme ve Görünürlük Mantığı ---
        gratitudeViewModel.getAllEntries().observe(this, entries -> {
            adapter.setEntries(entries);

            // Liste boşsa uyarıyı göster, değilse listeyi göstery
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