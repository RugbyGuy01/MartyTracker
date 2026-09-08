package com.golfpvcc.martytracker.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.golfpvcc.martytracker.domain.model.RoutePoint
import com.golfpvcc.martytracker.service.DistanceCalculator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlin.math.floor

private val ODD_MILE_COLOR = Color.Red
private val EVEN_MILE_COLOR = Color.Black

internal data class RouteSegment(val color: Color, val points: List<LatLng>)

// Splits the route into per-mile segments so each mile can be colored independently,
// alternating black/red starting with mile 1 = black. Boundary points are interpolated
// so segments join without gaps.
internal fun buildMileColoredSegments(routePoints: List<RoutePoint>): List<RouteSegment> {
    if (routePoints.size < 2) return emptyList()

    val segments = mutableListOf<RouteSegment>()
    var currentMile = 0
    var currentPoints = mutableListOf(LatLng(routePoints[0].lat, routePoints[0].lng))
    var cumulativeMiles = 0.0

    fun colorForMile(mile: Int) = if (mile % 2 == 0) EVEN_MILE_COLOR else ODD_MILE_COLOR

    for (i in 1 until routePoints.size) {
        val from = routePoints[i - 1]
        val to = routePoints[i]
        val stepMiles = DistanceCalculator.haversineMiles(from, to)
        val startCum = cumulativeMiles
        val endCum = cumulativeMiles + stepMiles

        var boundary = floor(startCum).toInt() + 1
        while (boundary <= floor(endCum).toInt() && stepMiles > 0.0) {
            val fraction = ((boundary - startCum) / stepMiles).coerceIn(0.0, 1.0)
            val interpolated = LatLng(
                from.lat + (to.lat - from.lat) * fraction,
                from.lng + (to.lng - from.lng) * fraction
            )
            currentPoints.add(interpolated)
            segments.add(RouteSegment(colorForMile(currentMile), currentPoints))
            currentMile = boundary
            currentPoints = mutableListOf(interpolated)
            boundary += 1
        }

        currentPoints.add(LatLng(to.lat, to.lng))
        cumulativeMiles = endCum
    }

    if (currentPoints.size >= 2) {
        segments.add(RouteSegment(colorForMile(currentMile), currentPoints))
    }
    return segments
}

@Composable
fun RoutePolylineMap(
    routePoints: List<RoutePoint>,
    modifier: Modifier = Modifier,
    startupLocation: RoutePoint? = null,
    followCurrentLocation: Boolean = false,
    uiSettings: MapUiSettings = MapUiSettings(zoomControlsEnabled = false)
) {
    val latLngPoints = routePoints.map { LatLng(it.lat, it.lng) }
    val startupLatLng = startupLocation?.let { LatLng(it.lat, it.lng) }
    val cameraPositionState = rememberCameraPositionState()
    var mapLoaded by remember { mutableStateOf(false) }

    // Before a walk starts (no route points yet), center on the phone's last known
    // location so the map isn't just a blank view of the ocean.
    if (routePoints.isEmpty() && startupLatLng != null) {
        LaunchedEffect(startupLatLng) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(startupLatLng, 17f)
        }
    } else if (followCurrentLocation) {
        LaunchedEffect(latLngPoints.lastOrNull()) {
            val target = latLngPoints.lastOrNull() ?: return@LaunchedEffect
            cameraPositionState.position = CameraPosition.fromLatLngZoom(target, 17f)
        }
    } else {
        // Viewing a finished route (e.g. History): fit the camera to the whole path
        // instead of zooming into just the last point, so all mile segments are visible.
        // Needs the map fully laid out (mapLoaded) before newLatLngBounds is safe to call.
        LaunchedEffect(latLngPoints, mapLoaded) {
            if (!mapLoaded || latLngPoints.isEmpty()) return@LaunchedEffect
            if (latLngPoints.size == 1) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLngPoints.first(), 17f)
                return@LaunchedEffect
            }
            val bounds = LatLngBounds.Builder().apply {
                latLngPoints.forEach { include(it) }
            }.build()
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = uiSettings,
        onMapLoaded = { mapLoaded = true }
    ) {
        for (segment in buildMileColoredSegments(routePoints)) {
            Polyline(points = segment.points, color = segment.color)
        }
        if (latLngPoints.isEmpty()) {
            startupLatLng?.let { location ->
                Marker(state = rememberUpdatedMarkerState(position = location), title = "Current location")
            }
        } else {
            latLngPoints.last().let { last ->
                Marker(state = rememberUpdatedMarkerState(position = last), title = "Current location")
            }
        }
    }
}
