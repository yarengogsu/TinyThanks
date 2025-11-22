package com.example.tinythanks;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WeekSummaryCalculator {

    private static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;
    private static final String PREFS_NAME = "tiny_thanks_prefs";
    private static final String KEY_DIFFICULT_PREFIX = "week_difficult_";

    /**
     * Calculates summary for a single week
     */
    public static WeekSummary calculateForWeek(List<GratitudeEntry> allEntries,
                                               long weekStartMillis,
                                               long weekEndMillis,
                                               boolean isDifficultWeek) {

        List<GratitudeEntry> weekEntries = new ArrayList<>();
        Set<Integer> activeDayIndexes = new HashSet<>();

        for (GratitudeEntry entry : allEntries) {
            long t = entry.getTimestamp();
            if (t >= weekStartMillis && t < weekEndMillis) {
                weekEntries.add(entry);

                int dayIndex = (int) ((t - weekStartMillis) / MILLIS_PER_DAY);
                activeDayIndexes.add(dayIndex);
            }
        }

        int total = weekEntries.size();
        int activeDays = activeDayIndexes.size();

        FlowerState state;

        // difficult => always wilted
        if (isDifficultWeek) {
            state = FlowerState.WILTED;
        } else if (total == 0) {
            state = FlowerState.BUD;
        } else if (total <= 3) {
            state = FlowerState.BUD;
        } else if (total <= 7) {
            state = FlowerState.PARTIAL;
        } else {
            state = FlowerState.FULL;
        }

        return new WeekSummary(
                weekStartMillis,
                weekEndMillis,
                total,
                activeDays,
                state,
                weekEntries
        );
    }

    /**
     * NEW METHOD — generates summaries for ALL weeks found in the DB.
     * FlowerGardenActivity bunu çağırıyor.
     */
    public static List<WeekSummary> calculateAllWeeks(Context context, List<GratitudeEntry> allEntries) {

        List<WeekSummary> result = new ArrayList<>();

        if (allEntries == null || allEntries.isEmpty()) {
            return result;
        }

        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Find earliest and latest timestamps
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        for (GratitudeEntry e : allEntries) {
            long t = e.getTimestamp();
            if (t < minTime) minTime = t;
            if (t > maxTime) maxTime = t;
        }

        // Snap earliest timestamp to start of its week
        long firstWeekStart = WeekUtils.getStartOfWeek(minTime);
        long currentWeekStart = firstWeekStart;

        while (currentWeekStart <= maxTime) {
            long currentWeekEnd = currentWeekStart + (7 * MILLIS_PER_DAY);

            boolean isDifficult =
                    prefs.getBoolean(KEY_DIFFICULT_PREFIX + currentWeekStart, false);

            WeekSummary ws = calculateForWeek(
                    allEntries,
                    currentWeekStart,
                    currentWeekEnd,
                    isDifficult
            );

            result.add(ws);

            currentWeekStart += (7 * MILLIS_PER_DAY);
        }

        // newest week first
        result.sort((a, b) -> Long.compare(b.getWeekStartMillis(), a.getWeekStartMillis()));

        return result;
    }
}
