package com.example.meeera.imageconverter

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

class ConverterApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.initInstance(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}