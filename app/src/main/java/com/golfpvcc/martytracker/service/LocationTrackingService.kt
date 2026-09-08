package com.golfpvcc.martytracker.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.golfpvcc.martytracker.domain.model.RoutePoint
import com.golfpvcc.martytracker.domain.repository.LiveTrackingRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.time.Instant

class LocationTrackingService : LifecycleService() {

    companion object {
        const val ACTION_START = "com.golfpvcc.martytracker.action.START"
        const val ACTION_PAUSE = "com.golfpvcc.martytracker.action.PAUSE"
        const val ACTION_RESUME = "com.golfpvcc.martytracker.action.RESUME"
        const val ACTION_STOP = "com.golfpvcc.martytracker.action.STOP"

        private const val UPDATE_INTERVAL_MS = 2000L
        private const val MAX_TONE_VOLUME = 100
        private const val MILE_MARKER_BEEP_DURATION_MS = 300
    }

    private val liveTrackingRepository: LiveTrackingRepository by inject()
    private val fusedLocationClient: FusedLocationProviderClient by inject()
    private lateinit var notificationHelper: TrackingNotificationHelper
    private lateinit var toneGenerator: ToneGenerator
    private var lastMileMarker = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            liveTrackingRepository.onLocationUpdate(
                RoutePoint(
                    lat = location.latitude,
                    lng = location.longitude,
                    timestamp = Instant.now(),
                    speedMph = location.speed * 2.23694
                )
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = TrackingNotificationHelper(this)
        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, MAX_TONE_VOLUME)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> {
                lastMileMarker = 0
                startForeground(
                    TrackingNotificationHelper.NOTIFICATION_ID,
                    notificationHelper.buildNotification(0.0, 0)
                )
                liveTrackingRepository.start()
                requestLocationUpdates()
                observeStatsForNotification()
            }
            ACTION_PAUSE -> {
                liveTrackingRepository.pause()
                removeLocationUpdates()
            }
            ACTION_RESUME -> {
                liveTrackingRepository.resume()
                requestLocationUpdates()
            }
            ACTION_STOP -> {
                liveTrackingRepository.stop()
                removeLocationUpdates()
                stopForeground(Service.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
    }

    private fun removeLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun observeStatsForNotification() {
        lifecycleScope.launch {
            combine(
                liveTrackingRepository.distanceMiles,
                liveTrackingRepository.elapsedSeconds
            ) { distance, elapsed -> distance to elapsed }
                .collect { (distance, elapsed) ->
                    checkMileMarker(distance)
                    NotificationManagerCompat.from(this@LocationTrackingService).notify(
                        TrackingNotificationHelper.NOTIFICATION_ID,
                        notificationHelper.buildNotification(distance, elapsed)
                    )
                }
        }
    }

    private fun checkMileMarker(distanceMiles: Double) {
        val milesReached = distanceMiles.toInt()
        if (milesReached > lastMileMarker) {
            lastMileMarker = milesReached
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, MILE_MARKER_BEEP_DURATION_MS)
        }
    }

    override fun onDestroy() {
        removeLocationUpdates()
        toneGenerator.release()
        super.onDestroy()
    }
}
