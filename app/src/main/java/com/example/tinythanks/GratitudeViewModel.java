package com.example.tinythanks;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class GratitudeViewModel extends AndroidViewModel {

    private final GratitudeRepository repository;
    private final LiveData<List<GratitudeEntry>> allGratitudes;

    public GratitudeViewModel(@NonNull Application application) {
        super(application);
        repository = new GratitudeRepository(application);

        // Repository'deki isimle birebir aynı olmalı: getAllGratitudes()
        allGratitudes = repository.getAllGratitudes();
    }

    // Listeyi Activity'ye göndermek için
    public LiveData<List<GratitudeEntry>> getAllEntries() {
        return allGratitudes;
    }

    // Yeni kayıt eklemek için
    public void insert(GratitudeEntry entry) {
        repository.insert(entry);
    }

    public void update(GratitudeEntry entry) {
        repository.update(entry);
    }
    // --- TASK METODLARI ---
    public LiveData<List<TaskEntry>> getAllTasks() {
        return repository.getAllTasks();
    }

    public void insertTask(TaskEntry task) {
        repository.insertTask(task);
    }

    public void updateTask(TaskEntry task) {
        repository.updateTask(task);
    }

    public void deleteTask(TaskEntry task) {
        repository.deleteTask(task);
    }
}