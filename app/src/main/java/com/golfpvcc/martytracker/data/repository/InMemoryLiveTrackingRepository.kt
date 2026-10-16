package com.golfpvcc.martytracker.data.repository

import com.golfpvcc.martytracker.domain.model.RoutePoint
import com.golfpvcc.martytracker.domain.model.TrackingStatus
import com.golfpvcc.martytracker.domain.model.Walk
import com.golfpvcc.martytracker.domain.repository.LiveTrackingRepository
import com.golfpvcc.martytracker.service.DistanceCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

private const val JITTER_THRESHOLD_MILES = 0.005

class InMemoryLiveTrackingRepository : LiveTrackingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null
    private var startInstant: Instant? = null
    private var distanceAnchor: RoutePoint? = null

    private val _status = MutableStateFlow(TrackingStatus.IDLE)
    override val status: StateFlow<TrackingStatus> = _status.asStateFlow()

    private val _routePoints = MutableStateFlow<List<RoutePoint>>(emptyList())
    override val routePoints: StateFlow<List<RoutePoint>> = _routePoints.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    override val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _distanceMiles = MutableStateFlow(0.0)
    override val distanceMiles: StateFlow<Double> = _distanceMiles.asStateFlow()

    private val _currentSpeedMph = MutableStateFlow(0.0)
    override val currentSpeedMph: StateFlow<Double> = _currentSpeedMph.asStateFlow()

    private val _avgSpeedMph = MutableStateFlow(0.0)
    override val avgSpeedMph: StateFlow<Double> = _avgSpeedMph.asStateFlow()

    private val _maxSpeedMph = MutableStateFlow(0.0)
    override val maxSpeedMph: StateFlow<Double> = _maxSpeedMph.asStateFlow()

    override fun onLocationUpdate(point: RoutePoint) {
        if (_status.value != TrackingStatus.TRACKING) return

        val anchor = distanceAnchor
        if (anchor == null) {
            distanceAnchor = point
        } else {
            val delta = DistanceCalculator.haversineMiles(anchor, point)
            if (delta >= JITTER_THRESHOLD_MILES) {
                _distanceMiles.value += delta
                distanceAnchor = point
            }
        }
        _routePoints.value = _routePoints.value + point
        _currentSpeedMph.value = point.speedMph
        if (point.speedMph > _maxSpeedMph.value) {
            _maxSpeedMph.value = point.speedMph
        }
        val hours = _elapsedSeconds.value / 3600.0
        _avgSpeedMph.value = if (hours > 0) _distanceMiles.value / hours else 0.0
    }

    override fun start() {
        if (_status.value == TrackingStatus.TRACKING) return
        if (startInstant == null) startInstant = Instant.now()
        _status.value = TrackingStatus.TRACKING
        startTimer()
    }

    override fun pause() {
        if (_status.value != TrackingStatus.TRACKING) return
        _status.value = TrackingStatus.PAUSED
        stopTimer()
    }

    override fun resume() {
        if (_status.value != TrackingStatus.PAUSED) return
        _status.value = TrackingStatus.TRACKING
        startTimer()
    }

    override fun stop(): Walk {
        stopTimer()
        _status.value = TrackingStatus.STOPPED
        return Walk(
            startTime = startInstant ?: Instant.now(),
            endTime = Instant.now(),
            distanceMiles = _distanceMiles.value,
            durationSeconds = _elapsedSeconds.value,
            avgSpeedMph = _avgSpeedMph.value,
            maxSpeedMph = _maxSpeedMph.value,
            route = _routePoints.value
        )
    }

    override fun reset() {
        stopTimer()
        startInstant = null
        distanceAnchor = null
        _status.value = TrackingStatus.IDLE
        _routePoints.value = emptyList()
        _elapsedSeconds.value = 0L
        _distanceMiles.value = 0.0
        _currentSpeedMph.value = 0.0
        _avgSpeedMph.value = 0.0
        _maxSpeedMph.value = 0.0
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value += 1
                val hours = _elapsedSeconds.value / 3600.0
                _avgSpeedMph.value = if (hours > 0) _distanceMiles.value / hours else 0.0
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
