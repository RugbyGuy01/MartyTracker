package com.golfpvcc.martytracker.di

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.golfpvcc.martytracker.service.TrackingServiceController
import org.koin.dsl.module

val serviceModule = module {
    single { LocationServices.getFusedLocationProviderClient(get<Context>()) }
    single { TrackingServiceController(get()) }
}
