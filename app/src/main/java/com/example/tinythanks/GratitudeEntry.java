package com.example.tinythanks;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "gratitude_table")
public class GratitudeEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String gratitudeText;
    private String photoPath;
    private long timestamp;
    private int mood; // 1=Kötü, 2=Orta, 3=İyi

    // Ana Constructor
    public GratitudeEntry(String gratitudeText, String photoPath, long timestamp, int mood) {
        this.gratitudeText = gratitudeText;
        this.photoPath = photoPath;
        this.timestamp = timestamp;
        this.mood = mood;
    }

    @Ignore
    public GratitudeEntry(int id, String gratitudeText, String photoPath, long timestamp, int mood) {
        this.id = id;
        this.gratitudeText = gratitudeText;
        this.photoPath = photoPath;
        this.timestamp = timestamp;
        this.mood = mood;
    }

    // Getter ve Setterlar
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGratitudeText() { return gratitudeText; }
    public String getPhotoPath() { return photoPath; }
    public long getTimestamp() { return timestamp; }

    public int getMood() { return mood; }
    public void setMood(int mood) { this.mood = mood; }
}