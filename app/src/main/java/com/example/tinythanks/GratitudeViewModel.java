package com.example.tinythanks;
import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
public class GratitudeViewModel extends AndroidViewModel{
    private GratitudeRepository repository;
    private final LiveData<List<GratitudeEntry>>allEntries;

    public GratitudeViewModel(Application application){
        super(application);
        repository=new GratitudeRepository(application);
        allEntries=repository.getAllGratitudeEntries();
    }
    public void insert(GratitudeEntry entry){
        repository.insert(entry);
    }
    public LiveData<List<GratitudeEntry>>getAllEntries(){
        return allEntries;
    }
}
