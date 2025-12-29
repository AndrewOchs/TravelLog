package com.example.travellog

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TravelLogApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-level components here
    }
}
