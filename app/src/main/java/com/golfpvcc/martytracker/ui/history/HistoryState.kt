package com.golfpvcc.martytracker.ui.history

import com.golfpvcc.martytracker.ui.history.model.WalkUi

data class HistoryState(
    val walks: List<WalkUi> = emptyList(),
    val isLoading: Boolean = true,
    val selectedWalk: WalkUi? = null
)
