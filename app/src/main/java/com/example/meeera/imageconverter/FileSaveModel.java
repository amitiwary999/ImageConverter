package com.example.meeera.imageconverter;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class FileSaveModel {

    @PrimaryKey(autoGenerate = true)
    int id;

    String fileDest;

}
