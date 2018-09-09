package com.example.meeera.imageconverter;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

public abstract class AppDatabase extends RoomDatabase{

    private static AppDatabase appDatabaseInstance;

    public void initInstance(Application application){
        if(appDatabaseInstance == null) {
            appDatabaseInstance = Room.databaseBuilder(application.getApplicationContext(), AppDatabase.class, "imageconverter").build();
        }
    }

    public static AppDatabase getAppDatabaseInstance(){
        return appDatabaseInstance;
    }
}
