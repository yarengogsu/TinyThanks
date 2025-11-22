package com.example.tinythanks;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class GratitudeRepository {

    private final GratitudeDao dao;
    private final LiveData<List<GratitudeEntry>> allEntries;

    public GratitudeRepository(Application application) {
        // DOĞRU database sınıfı: GratitudeDatabase
        GratitudeDatabase db = GratitudeDatabase.getDatabase(application);
        dao = db.gratitudeDao();
        allEntries = dao.getAllEntries();
    }

    public LiveData<List<GratitudeEntry>> getAllEntries() {
        return allEntries;
    }

    public void insert(GratitudeEntry entry) {
        GratitudeDatabase.databaseWriteExecutor.execute(() -> dao.insert(entry));
    }

    public void update(GratitudeEntry entry) {
        GratitudeDatabase.databaseWriteExecutor.execute(() -> dao.update(entry));
    }
}
