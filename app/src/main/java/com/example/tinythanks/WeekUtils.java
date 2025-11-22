package com.example.tinythanks;

public class WeekUtils {

    private static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

    // Monday as week start
    public static long getStartOfWeek(long timestamp) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        return cal.getTimeInMillis();
    }

    public static long getStartOfThisWeek() {
        return getStartOfWeek(System.currentTimeMillis());
    }

    public static long getEndOfWeek(long timestamp) {
        return getStartOfWeek(timestamp) + 7 * MILLIS_PER_DAY;
    }

    public static long getEndOfThisWeek() {
        return getStartOfThisWeek() + 7 * MILLIS_PER_DAY;
    }
}
