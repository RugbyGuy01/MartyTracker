package com.golfpvcc.martytracker.di

import androidx.room.Room
import com.golfpvcc.martytracker.data.local.db.MartyTrackerDatabase
import com.golfpvcc.martytracker.data.repository.InMemoryLiveTrackingRepository
import com.golfpvcc.martytracker.data.repository.RoomWalkRepository
import com.golfpvcc.martytracker.domain.repository.LiveTrackingRepository
import com.golfpvcc.martytracker.domain.repository.WalkRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(get(), MartyTrackerDatabase::class.java, "marty_tracker.db").build()
    }
    single { get<MartyTrackerDatabase>().walkDao() }
    single { get<MartyTrackerDatabase>().routePointDao() }
    single { RoomWalkRepository(get(), get(), get()) } bind WalkRepository::class
    single { InMemoryLiveTrackingRepository() } bind LiveTrackingRepository::class
}
