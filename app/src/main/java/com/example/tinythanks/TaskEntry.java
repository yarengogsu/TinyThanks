package com.example.tinythanks;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_table")
public class TaskEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;       // Görevin adı
    private boolean isCompleted; // Yapıldı mı? (Tik atıldı mı?)

    public TaskEntry(String title, boolean isCompleted) {
        this.title = title;
        this.isCompleted = isCompleted;
    }

    // Getter ve Setter Metotları
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}