package com.golfpvcc.martytracker.ui.history

sealed interface HistoryAction {
    data class OnWalkClick(val walkId: Long) : HistoryAction
    data object OnDismissDetail : HistoryAction
    data class OnDeleteWalk(val walkId: Long) : HistoryAction
}
