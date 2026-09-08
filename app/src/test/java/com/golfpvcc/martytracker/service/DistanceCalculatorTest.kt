package com.golfpvcc.martytracker.service

import com.golfpvcc.martytracker.domain.model.RoutePoint
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class DistanceCalculatorTest {

    private fun point(lat: Double, lng: Double) =
        RoutePoint(lat = lat, lng = lng, timestamp = Instant.EPOCH, speedMph = 0.0)

    @Test
    fun `same point has zero distance`() {
        val p = point(37.7749, -122.4194)
        assertEquals(0.0, DistanceCalculator.haversineMiles(p, p), 0.0001)
    }

    @Test
    fun `known distance between two coordinates is approximately correct`() {
        // San Francisco to Los Angeles is ~347 miles.
        val sf = point(37.7749, -122.4194)
        val la = point(34.0522, -118.2437)
        val miles = DistanceCalculator.haversineMiles(sf, la)
        assertEquals(347.0, miles, 5.0)
    }
}
