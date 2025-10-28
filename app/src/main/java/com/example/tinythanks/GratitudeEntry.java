package com.example.tinythanks;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
@Entity(tableName ="gratitude_table")
public class GratitudeEntry {
    @PrimaryKey(autoGenerate=true)
    private int id;

    private String gratitudeText;
    private String photoPath;
    private long timestamp;

    public void setId(int id) {
        this.id = id;
    }

    @Ignore
    public GratitudeEntry(String gratitudeText, String photoPath, int id, long timestamp) {
        this.gratitudeText = gratitudeText;
        this.photoPath = photoPath;
        this.id = id;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getGratitudeText() {
        return gratitudeText;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public GratitudeEntry(String gratitudeText, String photoPath, long timestamp){
        this.gratitudeText=gratitudeText;
        this.photoPath=photoPath;
        this.timestamp=timestamp;
    }
}
