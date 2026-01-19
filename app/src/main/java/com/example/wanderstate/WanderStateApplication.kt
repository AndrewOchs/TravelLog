package com.example.wanderstate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WanderStateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-level components here
    }
}
