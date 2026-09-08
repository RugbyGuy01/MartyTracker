package com.golfpvcc.martytracker.domain.repository

import com.golfpvcc.martytracker.domain.model.RoutePoint
import com.golfpvcc.martytracker.domain.model.TrackingStatus
import com.golfpvcc.martytracker.domain.model.Walk
import kotlinx.coroutines.flow.StateFlow

interface LiveTrackingRepository {
    val status: StateFlow<TrackingStatus>
    val routePoints: StateFlow<List<RoutePoint>>
    val elapsedSeconds: StateFlow<Long>
    val distanceMiles: StateFlow<Double>
    val currentSpeedMph: StateFlow<Double>
    val avgSpeedMph: StateFlow<Double>
    val maxSpeedMph: StateFlow<Double>

    fun onLocationUpdate(point: RoutePoint)
    fun start()
    fun pause()
    fun resume()
    fun stop(): Walk
    fun reset()
}
