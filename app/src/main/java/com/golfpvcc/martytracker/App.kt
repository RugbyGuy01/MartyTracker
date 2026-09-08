package com.golfpvcc.martytracker

import android.app.Application
import com.golfpvcc.martytracker.di.dataModule
import com.golfpvcc.martytracker.di.presentationModule
import com.golfpvcc.martytracker.di.serviceModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(dataModule, serviceModule, presentationModule)
        }
    }
}
