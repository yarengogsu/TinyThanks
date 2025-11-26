package com.example.tinythanks;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class GratitudeRepository {

    private GratitudeDao gratitudeDao;
    private LiveData<List<GratitudeEntry>> allGratitudes;

    // Constructor (Kurucu Metot)
    GratitudeRepository(Application application) {
        GratitudeDatabase db = GratitudeDatabase.getDatabase(application);
        gratitudeDao = db.gratitudeDao();

        // DAO dosyasında verdiğimiz isimle çağırıyoruz: getAllGratitudes()
        allGratitudes = gratitudeDao.getAllEntries();
    }

    // Tüm listeyi ViewModel'e göndermek için
    LiveData<List<GratitudeEntry>> getAllEntries() {
        return allGratitudes;
    }

    // Yeni kayıt eklemek için (Arka planda çalışır)
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
}