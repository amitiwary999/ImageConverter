package com.example.meeera.imageconverter;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface FileSaveRepository {
    @Insert
    long save(FileSaveModel fileSaveModel);

    @Query("SELECT * FROM FileSaveModel")
    List<FileSaveModel> getSavedFiles();
}
