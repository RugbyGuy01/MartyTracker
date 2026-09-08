package com.golfpvcc.martytracker.ui.common

import androidx.compose.ui.graphics.Color
import com.golfpvcc.martytracker.domain.model.RoutePoint
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutePolylineMapTest {

    // Roughly 0.01 miles of latitude per step, so 100 steps ~= 1 mile.
    private val milesPerDegreeLat = 69.0

    private fun buildStraightLineRoute(totalMiles: Double, stepsPerMile: Int = 50): List<RoutePoint> {
        val totalSteps = (totalMiles * stepsPerMile).toInt()
        val degreesPerStep = (1.0 / stepsPerMile) / milesPerDegreeLat
        return (0..totalSteps).map { i ->
            RoutePoint(
                lat = i * degreesPerStep,
                lng = 0.0,
                timestamp = Instant.EPOCH,
                speedMph = 3.0
            )
        }
    }

    @Test
    fun `route under one mile stays a single black segment`() {
        val route = buildStraightLineRoute(totalMiles = 0.5)
        val segments = buildMileColoredSegments(route)

        assertTrue(segments.isNotEmpty())
        assertTrue(segments.all { it.color == Color.Black })
    }

    @Test
    fun `route past three miles alternates black red black`() {
        val route = buildStraightLineRoute(totalMiles = 3.2)
        val segments = buildMileColoredSegments(route)

        val colors = segments.map { it.color }
        assertEquals(listOf(Color.Black, Color.Red, Color.Black, Color.Red), colors.distinctConsecutive())
    }

    private fun List<Color>.distinctConsecutive(): List<Color> {
        val result = mutableListOf<Color>()
        for (c in this) if (result.lastOrNull() != c) result.add(c)
        return result
    }
}
