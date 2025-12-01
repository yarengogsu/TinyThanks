package com.example.tinythanks;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface GratitudeDao {

    // --- GRATITUDE (ŞÜKÜR) KISMI ---
    @Insert
    void insert(GratitudeEntry entry);

    @Update
    void update(GratitudeEntry entry);

    @Query("SELECT * FROM gratitude_table ORDER BY timestamp DESC")
    LiveData<List<GratitudeEntry>> getAllGratitudes();

    // --- YENİ EKLENEN: TASK (GÖREV) KISMI ---
    @Insert
    void insertTask(TaskEntry task);

    @Update
    void updateTask(TaskEntry task);

    @Delete
    void deleteTask(TaskEntry task);

    @Query("SELECT * FROM task_table ORDER BY id DESC")
    LiveData<List<TaskEntry>> getAllTasks();
}