package com.example.tinythanks;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class GratitudeRepository {

    private GratitudeDao gratitudeDao;
    private LiveData<List<GratitudeEntry>> allGratitudes;
    private LiveData<List<TaskEntry>> allTasks; // Görevler için

    // Constructor
    GratitudeRepository(Application application) {
        GratitudeDatabase db = GratitudeDatabase.getDatabase(application);
        gratitudeDao = db.gratitudeDao();

        // DİKKAT: Dao dosyasındaki isimle aynı olmalı (getAllGratitudes)
        allGratitudes = gratitudeDao.getAllGratitudes();

        // Görevleri de alalım
        allTasks = gratitudeDao.getAllTasks();
    }

    // --- GRATITUDE (ŞÜKÜR) METODLARI ---

    // ViewModel bu ismi kullanacak: getAllGratitudes
    LiveData<List<GratitudeEntry>> getAllGratitudes() {
        return allGratitudes;
    }

    void insert(GratitudeEntry entry) {
        GratitudeDatabase.databaseWriteExecutor.execute(() -> {
            gratitudeDao.insert(entry);
        });
    }

    void update(GratitudeEntry entry) {
        GratitudeDatabase.databaseWriteExecutor.execute(() -> {
            gratitudeDao.update(entry);
        });
    }

    // --- TASK (GÖREV) METODLARI ---

    LiveData<List<TaskEntry>> getAllTasks() {
        return allTasks;
    }

    void insertTask(TaskEntry task) {
        GratitudeDatabase.databaseWriteExecutor.execute(() -> {
            gratitudeDao.insertTask(task);
        });
    }

    void updateTask(TaskEntry task) {
        GratitudeDatabase.databaseWriteExecutor.execute(() -> {
            gratitudeDao.updateTask(task);
        });
    }

    void deleteTask(TaskEntry task) {
        GratitudeDatabase.databaseWriteExecutor.execute(() -> {
            gratitudeDao.deleteTask(task);
        });
    }
}