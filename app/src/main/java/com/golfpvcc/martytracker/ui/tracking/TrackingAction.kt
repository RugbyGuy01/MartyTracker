package com.golfpvcc.martytracker.ui.tracking

sealed interface TrackingAction {
    data object OnStartClick : TrackingAction
    data object OnPauseClick : TrackingAction
    data object OnResumeClick : TrackingAction
    data object OnStopClick : TrackingAction
    data object OnSaveWalkClick : TrackingAction
    data object OnDiscardWalkClick : TrackingAction
}
