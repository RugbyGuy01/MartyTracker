package com.golfpvcc.martytracker.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.golfpvcc.martytracker.data.local.db.entity.WalkEntity
import com.golfpvcc.martytracker.data.local.db.relation.WalkWithRoutePoints
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkDao {
    @Insert
    suspend fun insertWalk(walk: WalkEntity): Long

    @Transaction
    @Query("SELECT * FROM walks ORDER BY startTime DESC")
    fun getAllWalksWithRoute(): Flow<List<WalkWithRoutePoints>>

    @Transaction
    @Query("SELECT * FROM walks WHERE id = :id")
    suspend fun getWalkWithRoute(id: Long): WalkWithRoutePoints?

    @Query("DELETE FROM walks WHERE id = :id")
    suspend fun deleteWalk(id: Long)
}
