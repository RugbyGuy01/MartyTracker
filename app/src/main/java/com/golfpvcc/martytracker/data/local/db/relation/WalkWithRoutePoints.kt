package com.golfpvcc.martytracker.data.local.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.golfpvcc.martytracker.data.local.db.entity.RoutePointEntity
import com.golfpvcc.martytracker.data.local.db.entity.WalkEntity

data class WalkWithRoutePoints(
    @Embedded val walk: WalkEntity,
    @Relation(parentColumn = "id", entityColumn = "walkId")
    val routePoints: List<RoutePointEntity>
)
