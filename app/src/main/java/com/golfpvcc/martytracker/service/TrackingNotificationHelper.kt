package com.golfpvcc.martytracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.golfpvcc.martytracker.MainActivity
import com.golfpvcc.martytracker.R

class TrackingNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "walk_tracking"
        const val NOTIFICATION_ID = 1001
    }

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Walk tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows live stats while a walk is being tracked"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun buildNotification(distanceMiles: Double, elapsedSeconds: Long): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val distanceText = String.format("%.2f mi", distanceMiles)
        val timeText = String.format("%02d:%02d", minutes, seconds)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Tracking your walk")
            .setContentText("$distanceText • $timeText")
            .setSmallIcon(R.drawable.ic_home)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .build()
    }
}
