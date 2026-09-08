package com.golfpvcc.martytracker.ui.history.model

import com.golfpvcc.martytracker.domain.model.RoutePoint

data class WalkUi(
    val id: Long,
    val dateLabel: String,
    val distanceLabel: String,
    val durationLabel: String,
    val avgSpeedLabel: String,
    val maxSpeedLabel: String,
    val route: List<RoutePoint>
)
