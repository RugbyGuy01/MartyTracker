package com.golfpvcc.martytracker.ui

fun formatDistance(miles: Double): String = String.format("%.2f mi", miles)

fun formatSpeed(mph: Double): String = String.format("%.1f mph", mph)

fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
