package com.example.tinythanks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class GratitudeViewModel extends AndroidViewModel {

    private final GratitudeRepository repository;
    private final LiveData<List<GratitudeEntry>> allEntries;

    public GratitudeViewModel(@NonNull Application application) {
        super(application);
        repository = new GratitudeRepository(application);
        allEntries = repository.getAllEntries();   // <-- BURASI ÖNEMLİ
    }

    public LiveData<List<GratitudeEntry>> getAllEntries() {
        return allEntries;
    }

    public void insert(GratitudeEntry entry) {
        repository.insert(entry);
    }

    public void update(GratitudeEntry entry) {
        repository.update(entry);
    }
}
