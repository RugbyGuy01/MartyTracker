package com.golfpvcc.martytracker.ui.tracking

sealed interface TrackingEvent {
    data object WalkSaved : TrackingEvent
}
