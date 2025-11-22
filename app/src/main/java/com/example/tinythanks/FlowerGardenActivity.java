package com.example.tinythanks;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shows the "flower garden" = list of week summaries.
 * Her satır bir WeekSummary, solda mini FlowerView ile.
 */
public class FlowerGardenActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "tiny_thanks_prefs";
    private static final String KEY_DIFFICULT_PREFIX = "week_difficult_";

    private GratitudeViewModel gratitudeViewModel;
    private WeekSummaryAdapter adapter;

    private List<GratitudeEntry> lastEntries = Collections.emptyList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower_garden);

        RecyclerView recyclerView = findViewById(R.id.recycler_weeks);
        adapter = new WeekSummaryAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        gratitudeViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(GratitudeViewModel.class);

        // DB entry'lerini dinle
        gratitudeViewModel.getAllEntries().observe(this, entries -> {
            if (entries == null) {
                lastEntries = Collections.emptyList();
            } else {
                lastEntries = entries;
            }
            updateGarden();   // ilk yüklemede doldur
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // WeeklyFlowerActivity'de difficult flag değişmiş olabilir.
        // Aynı entry listesiyle haftaları yeniden hesapla.
        updateGarden();
    }

    /**
     * lastEntries listesini kullanarak bütün haftaların özetini hesaplar ve adapter'e verir.
     */
    private void updateGarden() {
        List<WeekSummary> summaries = buildWeekSummaries(lastEntries);
        adapter.setWeeks(summaries);
    }

    /**
     * Tüm entry'lerden hafta bazlı özetler üretir.
     * Difficult bilgisi SharedPreferences'tan okunur.
     */
    private List<WeekSummary> buildWeekSummaries(List<GratitudeEntry> entries) {
        List<WeekSummary> result = new ArrayList<>();
        if (entries == null || entries.isEmpty()) {
            return result;
        }

        // min / max time
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        for (GratitudeEntry e : entries) {
            long t = e.getTimestamp();
            if (t < minTime) minTime = t;
            if (t > maxTime) maxTime = t;
        }

        long currentWeekStart = WeekUtils.getStartOfWeek(minTime);

        while (currentWeekStart <= maxTime) {
            long currentWeekEnd = currentWeekStart + 7L * 24L * 60L * 60L * 1000L;

            boolean isDifficult = isWeekMarkedDifficult(currentWeekStart);

            WeekSummary summary = WeekSummaryCalculator.calculateForWeek(
                    entries,
                    currentWeekStart,
                    currentWeekEnd,
                    isDifficult
            );
            result.add(summary);

            currentWeekStart += 7L * 24L * 60L * 60L * 1000L;
        }

        // yeni hafta üstte
        result.sort((a, b) ->
                Long.compare(b.getWeekStartMillis(), a.getWeekStartMillis()));

        return result;
    }

    private boolean isWeekMarkedDifficult(long weekStartMillis) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String key = KEY_DIFFICULT_PREFIX + weekStartMillis;
        return prefs.getBoolean(key, false);
    }
}
