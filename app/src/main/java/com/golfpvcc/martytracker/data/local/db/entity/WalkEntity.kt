package com.golfpvcc.martytracker.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walks")
data class WalkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val distanceMiles: Double,
    val durationSeconds: Long,
    val avgSpeedMph: Double,
    val maxSpeedMph: Double
)
