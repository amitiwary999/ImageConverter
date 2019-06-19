package com.example.meeera.imageconverter


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FileSaveRepository {

    @Query("SELECT * FROM FileSaveModel")
    suspend fun savedFiles(): List<FileSaveModel>

    @Insert
    suspend fun save(fileSaveModel: FileSaveModel): Long
}
