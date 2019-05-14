package com.example.meeera.imageconverter;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class FileSaveModel {

    @PrimaryKey(autoGenerate = true)
    int id;

    String fileDest;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileDest() {
        return fileDest;
    }

    public void setFileDest(String fileDest) {
        this.fileDest = fileDest;
    }
}
