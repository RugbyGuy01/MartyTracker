package com.golfpvcc.martytracker.service

import com.golfpvcc.martytracker.domain.model.RoutePoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {
    private const val EARTH_RADIUS_MILES = 3958.8

    fun haversineMiles(from: RoutePoint, to: RoutePoint): Double {
        val dLat = Math.toRadians(to.lat - from.lat)
        val dLng = Math.toRadians(to.lng - from.lng)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(from.lat)) * cos(Math.toRadians(to.lat)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_MILES * c
    }
}
