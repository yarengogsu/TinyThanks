package com.example.tinythanks;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface GratitudeDao {
    @Insert
    void insert(GratitudeEntry entry);

    @Query("SELECT * FROM gratitude_table ORDER BY timestamp DESC")
    LiveData<List<GratitudeEntry>> getAllEntries();

    @Update
    void update(GratitudeEntry entry);

    @Query("DELETE FROM gratitude_table")
    void deleteAll();
}