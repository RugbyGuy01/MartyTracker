package com.golfpvcc.martytracker.ui.tracking

import com.golfpvcc.martytracker.domain.model.RoutePoint
import com.golfpvcc.martytracker.domain.model.TrackingStatus

data class TrackingState(
    val status: TrackingStatus = TrackingStatus.IDLE,
    val routePoints: List<RoutePoint> = emptyList(),
    val elapsedSeconds: Long = 0,
    val distanceMiles: Double = 0.0,
    val currentSpeedMph: Double = 0.0,
    val avgSpeedMph: Double = 0.0,
    val maxSpeedMph: Double = 0.0,
    val showSummaryDialog: Boolean = false,
    val startupLocation: RoutePoint? = null
) {
    val currentLocation: RoutePoint? get() = routePoints.lastOrNull() ?: startupLocation
}
