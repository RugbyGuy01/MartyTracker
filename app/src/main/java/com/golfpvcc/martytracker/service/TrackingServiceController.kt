package com.golfpvcc.martytracker.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class TrackingServiceController(private val context: Context) {

    fun start() = send(LocationTrackingService.ACTION_START)
    fun pause() = send(LocationTrackingService.ACTION_PAUSE)
    fun resume() = send(LocationTrackingService.ACTION_RESUME)
    fun stop() = send(LocationTrackingService.ACTION_STOP)

    private fun send(action: String) {
        val intent = Intent(context, LocationTrackingService::class.java).setAction(action)
        ContextCompat.startForegroundService(context, intent)
    }
}
