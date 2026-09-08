package com.golfpvcc.martytracker.domain.model

import java.time.Instant

data class Walk(
    val id: Long = 0L,
    val startTime: Instant,
    val endTime: Instant,
    val distanceMiles: Double,
    val durationSeconds: Long,
    val avgSpeedMph: Double,
    val maxSpeedMph: Double,
    val route: List<RoutePoint> = emptyList()
)
