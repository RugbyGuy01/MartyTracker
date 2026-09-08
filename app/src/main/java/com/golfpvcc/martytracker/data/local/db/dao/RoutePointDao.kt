package com.golfpvcc.martytracker.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.golfpvcc.martytracker.data.local.db.entity.RoutePointEntity

@Dao
interface RoutePointDao {
    @Insert
    suspend fun insertAll(points: List<RoutePointEntity>)
}
