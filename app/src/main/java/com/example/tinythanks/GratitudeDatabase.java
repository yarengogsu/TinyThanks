package com.example.tinythanks;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities={GratitudeEntry.class},version=1,exportSchema = false)
public abstract class GratitudeDatabase extends RoomDatabase{
    public abstract GratitudeDao gratitudeDao();

    private static volatile GratitudeDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS=4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    public static GratitudeDatabase getDatabase(final Context context){
        if(INSTANCE==null){
            INSTANCE=Room.databaseBuilder(context.getApplicationContext(),GratitudeDatabase.class,"gratitude_database").build();

        }
        return INSTANCE;
    }
}
