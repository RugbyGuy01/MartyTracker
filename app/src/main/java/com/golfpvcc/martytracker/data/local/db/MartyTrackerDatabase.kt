package com.golfpvcc.martytracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.golfpvcc.martytracker.data.local.db.dao.RoutePointDao
import com.golfpvcc.martytracker.data.local.db.dao.WalkDao
import com.golfpvcc.martytracker.data.local.db.entity.RoutePointEntity
import com.golfpvcc.martytracker.data.local.db.entity.WalkEntity

@Database(
    entities = [WalkEntity::class, RoutePointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MartyTrackerDatabase : RoomDatabase() {
    abstract fun walkDao(): WalkDao
    abstract fun routePointDao(): RoutePointDao
}
