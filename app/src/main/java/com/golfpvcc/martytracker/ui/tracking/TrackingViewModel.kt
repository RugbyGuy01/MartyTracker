package com.golfpvcc.martytracker.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfpvcc.martytracker.domain.model.RoutePoint
import com.golfpvcc.martytracker.domain.model.TrackingStatus
import com.golfpvcc.martytracker.domain.model.Walk
import com.golfpvcc.martytracker.domain.repository.LiveTrackingRepository
import com.golfpvcc.martytracker.domain.repository.WalkRepository
import com.golfpvcc.martytracker.service.TrackingServiceController
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant

class TrackingViewModel(
    private val liveTrackingRepository: LiveTrackingRepository,
    private val walkRepository: WalkRepository,
    private val serviceController: TrackingServiceController,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    private val _events = Channel<TrackingEvent>()
    val events = _events.receiveAsFlow()

    private var pendingWalk: Walk? = null

    init {
        loadStartupLocation()
        viewModelScope.launch {
            combine(
                liveTrackingRepository.status,
                liveTrackingRepository.routePoints,
                liveTrackingRepository.elapsedSeconds,
                liveTrackingRepository.distanceMiles,
                liveTrackingRepository.currentSpeedMph,
                liveTrackingRepository.avgSpeedMph,
                liveTrackingRepository.maxSpeedMph
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                TrackingSnapshot(
                    status = values[0] as TrackingStatus,
                    routePoints = values[1] as List<RoutePoint>,
                    elapsedSeconds = values[2] as Long,
                    distanceMiles = values[3] as Double,
                    currentSpeedMph = values[4] as Double,
                    avgSpeedMph = values[5] as Double,
                    maxSpeedMph = values[6] as Double
                )
            }.collect { snapshot ->
                _state.update {
                    it.copy(
                        status = snapshot.status,
                        routePoints = snapshot.routePoints,
                        elapsedSeconds = snapshot.elapsedSeconds,
                        distanceMiles = snapshot.distanceMiles,
                        currentSpeedMph = snapshot.currentSpeedMph,
                        avgSpeedMph = snapshot.avgSpeedMph,
                        maxSpeedMph = snapshot.maxSpeedMph
                    )
                }
            }
        }
    }

    fun onLocationPermissionGranted() {
        loadStartupLocation()
    }

    private fun loadStartupLocation() {
        viewModelScope.launch {
            try {
                fusedLocationClient.lastLocation.await()?.let { location ->
                    _state.update {
                        it.copy(
                            startupLocation = RoutePoint(
                                lat = location.latitude,
                                lng = location.longitude,
                                timestamp = Instant.now(),
                                speedMph = 0.0
                            )
                        )
                    }
                }
            } catch (e: SecurityException) {
                // Location permission not granted yet; map falls back to its default view.
            }
        }
    }

    fun onAction(action: TrackingAction) {
        when (action) {
            TrackingAction.OnStartClick -> serviceController.start()
            TrackingAction.OnPauseClick -> serviceController.pause()
            TrackingAction.OnResumeClick -> serviceController.resume()
            TrackingAction.OnStopClick -> {
                pendingWalk = liveTrackingRepository.stop()
                serviceController.stop()
                _state.update { it.copy(showSummaryDialog = true) }
            }
            TrackingAction.OnSaveWalkClick -> {
                val walk = pendingWalk ?: return
                viewModelScope.launch {
                    walkRepository.saveWalk(walk)
                    pendingWalk = null
                    liveTrackingRepository.reset()
                    _state.update { it.copy(showSummaryDialog = false) }
                    _events.send(TrackingEvent.WalkSaved)
                }
            }
            TrackingAction.OnDiscardWalkClick -> {
                pendingWalk = null
                liveTrackingRepository.reset()
                _state.update { it.copy(showSummaryDialog = false) }
            }
        }
    }

    private data class TrackingSnapshot(
        val status: TrackingStatus,
        val routePoints: List<RoutePoint>,
        val elapsedSeconds: Long,
        val distanceMiles: Double,
        val currentSpeedMph: Double,
        val avgSpeedMph: Double,
        val maxSpeedMph: Double
    )
}
