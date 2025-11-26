package com.example.tinythanks;

public class CalendarDay {

    private final long dayStartMillis;   // o günün 00:00'ı
    private final boolean hasEntries;
    private final int dayOfMonth;
    private final String thumbnailPhotoPath;

    public CalendarDay(long dayStartMillis,
                       boolean hasEntries,
                       int dayOfMonth,
                       String thumbnailPhotoPath) {
        this.dayStartMillis = dayStartMillis;
        this.hasEntries = hasEntries;
        this.dayOfMonth = dayOfMonth;
        this.thumbnailPhotoPath = thumbnailPhotoPath;
    }

    public long getDayStartMillis() {
        return dayStartMillis;
    }

    public boolean hasEntries() {
        return hasEntries;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public String getThumbnailPhotoPath() {
        return thumbnailPhotoPath;
    }
}
