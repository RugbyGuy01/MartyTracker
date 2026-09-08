package com.golfpvcc.martytracker.data.local.mapper

import com.golfpvcc.martytracker.data.local.db.entity.RoutePointEntity
import com.golfpvcc.martytracker.data.local.db.entity.WalkEntity
import com.golfpvcc.martytracker.data.local.db.relation.WalkWithRoutePoints
import com.golfpvcc.martytracker.domain.model.RoutePoint
import com.golfpvcc.martytracker.domain.model.Walk
import java.time.Instant

fun WalkWithRoutePoints.toDomain(): Walk = Walk(
    id = walk.id,
    startTime = Instant.ofEpochMilli(walk.startTime),
    endTime = Instant.ofEpochMilli(walk.endTime),
    distanceMiles = walk.distanceMiles,
    durationSeconds = walk.durationSeconds,
    avgSpeedMph = walk.avgSpeedMph,
    maxSpeedMph = walk.maxSpeedMph,
    route = routePoints.map { it.toDomain() }
)

fun RoutePointEntity.toDomain(): RoutePoint = RoutePoint(
    lat = lat,
    lng = lng,
    timestamp = Instant.ofEpochMilli(timestamp),
    speedMph = speedMph
)

fun Walk.toEntity(): WalkEntity = WalkEntity(
    id = id,
    startTime = startTime.toEpochMilli(),
    endTime = endTime.toEpochMilli(),
    distanceMiles = distanceMiles,
    durationSeconds = durationSeconds,
    avgSpeedMph = avgSpeedMph,
    maxSpeedMph = maxSpeedMph
)

fun RoutePoint.toEntity(walkId: Long): RoutePointEntity = RoutePointEntity(
    walkId = walkId,
    lat = lat,
    lng = lng,
    timestamp = timestamp.toEpochMilli(),
    speedMph = speedMph
)
