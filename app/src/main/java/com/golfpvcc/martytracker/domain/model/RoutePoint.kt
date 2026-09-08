package com.golfpvcc.martytracker.domain.model

import java.time.Instant

data class RoutePoint(
    val lat: Double,
    val lng: Double,
    val timestamp: Instant,
    val speedMph: Double
)
