package com.example.tinythanks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Weekly flower screen:
 * - uses REAL Room data via GratitudeViewModel
 * - calculates weekly summary
 * - draws the flower
 * - shows only this week's entries in a list
 * - allows toggling "this week was difficult" on/off with one button
 * - can show either "this week" or a specific week passed via Intent
 */
public class WeeklyFlowerActivity extends AppCompatActivity {

    // Intent extra key: if set, this activity will show that specific week
    public static final String EXTRA_WEEK_START = "extra_week_start";

    private static final String PREFS_NAME = "tiny_thanks_prefs";
    private static final String KEY_DIFFICULT_PREFIX = "week_difficult_";

    private FlowerView flowerView;
    private TextView totalEntriesText;
    private TextView activeDaysText;
    private TextView flowerStateText;
    private Button toggleButton;

    private RecyclerView weekRecyclerView;
    private GratitudeAdapter weekAdapter;

    private GratitudeViewModel gratitudeViewModel;

    private long weekStartMillis;
    private long weekEndMillis;

    private boolean isDifficultWeek = false;
    private WeekSummary summary;
    private List<GratitudeEntry> currentEntries = Collections.emptyList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_flower);

        // --- bind views ---
        flowerView = findViewById(R.id.flower_view);
        totalEntriesText = findViewById(R.id.text_total_entries);
        activeDaysText = findViewById(R.id.text_active_days);
        flowerStateText = findViewById(R.id.text_flower_state);
        toggleButton = findViewById(R.id.btn_toggle_week_state);

        weekRecyclerView = findViewById(R.id.recycler_week_entries);
        weekAdapter = new GratitudeAdapter(this);
        weekRecyclerView.setAdapter(weekAdapter);
        weekRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- ViewModel (REAL Room data) ---
        gratitudeViewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(GratitudeViewModel.class);

        // --- calculate week range ---
        // If a specific week was passed from FlowerGarden, use that.
        // Otherwise, default to "this week".
        long passedWeekStart = getIntent().getLongExtra(EXTRA_WEEK_START, -1);

        if (passedWeekStart != -1) {
            // show specific week (from FlowerGarden)
            weekStartMillis = passedWeekStart;
            weekEndMillis = WeekUtils.getEndOfWeek(passedWeekStart);
        } else {
            // default: show this week
            weekStartMillis = WeekUtils.getStartOfThisWeek();
            weekEndMillis = WeekUtils.getEndOfThisWeek();
        }

        // read saved difficult state for this week
        isDifficultWeek = isWeekMarkedDifficult(weekStartMillis);

        // --- observe REAL Room LiveData ---
        gratitudeViewModel.getAllEntries().observe(this, entries -> {
            currentEntries = (entries != null) ? entries : Collections.emptyList();

            summary = WeekSummaryCalculator.calculateForWeek(
                    currentEntries,
                    weekStartMillis,
                    weekEndMillis,
                    isDifficultWeek
            );

            updateUIFromSummary();
            updateButtonLabel();
        });

        // --- toggle button: difficult <-> normal ---
        toggleButton.setOnClickListener(v -> {
            // flip the flag
            isDifficultWeek = !isDifficultWeek;

            // save to SharedPreferences
            markWeekDifficult(weekStartMillis, isDifficultWeek);

            // recalculate summary with new difficult flag
            summary = WeekSummaryCalculator.calculateForWeek(
                    currentEntries,
                    weekStartMillis,
                    weekEndMillis,
                    isDifficultWeek
            );

            updateUIFromSummary();
            updateButtonLabel();
        });
    }

    /**
     * Updates the text labels, the flower drawing, and the weekly list.
     */
    private void updateUIFromSummary() {
        if (summary == null) {
            totalEntriesText.setText("Total entries: 0");
            activeDaysText.setText("Active days: 0");
            flowerStateText.setText("Flower state: BUD");

            flowerView.setFlowerState(FlowerState.BUD);
            flowerView.setTotalEntries(0);

            weekAdapter.setEntries(Collections.emptyList());
            return;
        }

        totalEntriesText.setText("Total entries: " + summary.getTotalEntries());
        activeDaysText.setText("Active days: " + summary.getActiveDaysCount());
        flowerStateText.setText("Flower state: " + summary.getFlowerState());

        flowerView.setFlowerState(summary.getFlowerState());
        flowerView.setTotalEntries(summary.getTotalEntries());

        // only this week's entries in the list
        if (summary.getEntries() != null) {
            weekAdapter.setEntries(summary.getEntries());
        } else {
            weekAdapter.setEntries(Collections.emptyList());
        }
    }

    /**
     * Updates the toggle button text depending on the current difficult state.
     */
    private void updateButtonLabel() {
        if (isDifficultWeek) {
            toggleButton.setText("This week was actually OK 😊");
        } else {
            toggleButton.setText("This week was difficult 😔");
        }
    }

    /**
     * Reads from SharedPreferences if this week was marked as difficult.
     */
    private boolean isWeekMarkedDifficult(long weekStartMillis) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String key = KEY_DIFFICULT_PREFIX + weekStartMillis;
        return prefs.getBoolean(key, false);
    }

    /**
     * Saves "this week is difficult" information to SharedPreferences.
     */
    private void markWeekDifficult(long weekStartMillis, boolean value) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String key = KEY_DIFFICULT_PREFIX + weekStartMillis;
        prefs.edit().putBoolean(key, value).apply();
    }
}
