package com.example.tinythanks;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import java.util.Calendar;

public class JourneyActivity extends AppCompatActivity {

    private GratitudeViewModel gratitudeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        // Calendar Settings
        MaterialCalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setSelectedDate(Calendar.getInstance());

        // --- (FAB) ---
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(JourneyActivity.this, AddGratitudeActivity.class));
        });

        // Bottom Nav Settings
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // (History/Journey)
        bottomNav.setSelectedItemId(R.id.nav_history);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // 1. Home
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (id == R.id.nav_history) {

                return true;
            }
            else if (id == R.id.nav_ideas) {
                // 3. Go Ideas Page
                startActivity(new Intent(getApplicationContext(), IdeasActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (id == R.id.nav_profile) {
                // 4. Go to the Profile page
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        // List Settings
        RecyclerView recyclerView = findViewById(R.id.rvJourneyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final JourneyAdapter adapter = new JourneyAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(entry -> {
            // Tıklanan kaydı düzenlemek için AddGratitudeActivity'yi açıyoruz
            Intent intent = new Intent(JourneyActivity.this, AddGratitudeActivity.class);

            // Verileri çantaya koyup gönderiyoruz
            intent.putExtra("EXTRA_ID", entry.getId());
            intent.putExtra("EXTRA_CONTENT", entry.getGratitudeText());
            intent.putExtra("EXTRA_IMAGE_PATH", entry.getPhotoPath());
            intent.putExtra("EXTRA_TIMESTAMP", entry.getTimestamp());

            startActivity(intent);
        });
        // Database Connection
        gratitudeViewModel = new ViewModelProvider(this).get(GratitudeViewModel.class);
        gratitudeViewModel.getAllEntries().observe(this, entries -> {
            adapter.setEntries(entries);
        });
    }
}