package com.example.tinythanks;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class GratitudeRepository {
    private GratitudeDao gratitudeDao;
    private LiveData<List<GratitudeEntry>>allGratitudeEntries;

    public GratitudeRepository(Application application){
        GratitudeDatabase db=GratitudeDatabase.getDatabase(application);
        gratitudeDao=db.gratitudeDao();
        allGratitudeEntries=gratitudeDao.getAllEntries();
    }
    public void insert(GratitudeEntry entry){
        GratitudeDatabase.databaseWriteExecutor.execute(()->{
            gratitudeDao.insert(entry);
        });
    }
    public LiveData<List<GratitudeEntry>>getAllGratitudeEntries(){
        return allGratitudeEntries;
    }
}
