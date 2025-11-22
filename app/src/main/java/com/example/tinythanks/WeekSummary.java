package com.example.tinythanks;

import java.util.List;

/**
 * Holds summary information for a single week.
 * This will be used by the weekly flower screen.
 */
public class WeekSummary {

    private long weekStartMillis;          // start of week (in millis)
    private long weekEndMillis;            // end of week (in millis)
    private int totalEntries;              // how many gratitude entries in this week
    private int activeDaysCount;           // on how many different days user wrote something
    private FlowerState flowerState;       // visual state of the flower
    private List<GratitudeEntry> entries;  // all entries for this week

    public WeekSummary(long weekStartMillis,
                       long weekEndMillis,
                       int totalEntries,
                       int activeDaysCount,
                       FlowerState flowerState,
                       List<GratitudeEntry> entries) {
        this.weekStartMillis = weekStartMillis;
        this.weekEndMillis = weekEndMillis;
        this.totalEntries = totalEntries;
        this.activeDaysCount = activeDaysCount;
        this.flowerState = flowerState;
        this.entries = entries;
    }

    public long getWeekStartMillis() {
        return weekStartMillis;
    }

    public long getWeekEndMillis() {
        return weekEndMillis;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public int getActiveDaysCount() {
        return activeDaysCount;
    }

    public FlowerState getFlowerState() {
        return flowerState;
    }

    public List<GratitudeEntry> getEntries() {
        return entries;
    }

    public void setFlowerState(FlowerState flowerState) {
        this.flowerState = flowerState;
    }
}
