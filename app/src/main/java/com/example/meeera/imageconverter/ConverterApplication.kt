package com.example.meeera.imageconverter

import android.app.Application

class ConverterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.initInstance(this)
    }

}