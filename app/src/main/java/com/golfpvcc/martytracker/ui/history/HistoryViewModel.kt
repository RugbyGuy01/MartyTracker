package com.golfpvcc.martytracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfpvcc.martytracker.domain.model.Walk
import com.golfpvcc.martytracker.domain.repository.WalkRepository
import com.golfpvcc.martytracker.ui.formatDistance
import com.golfpvcc.martytracker.ui.formatDuration
import com.golfpvcc.martytracker.ui.formatSpeed
import com.golfpvcc.martytracker.ui.history.model.WalkUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")

class HistoryViewModel(
    private val walkRepository: WalkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            walkRepository.getAllWalks().collect { walks ->
                _state.update { it.copy(walks = walks.map(::toUi), isLoading = false) }
            }
        }
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.OnWalkClick -> {
                val walk = _state.value.walks.firstOrNull { it.id == action.walkId }
                _state.update { it.copy(selectedWalk = walk) }
            }
            HistoryAction.OnDismissDetail -> _state.update { it.copy(selectedWalk = null) }
            is HistoryAction.OnDeleteWalk -> viewModelScope.launch {
                walkRepository.deleteWalk(action.walkId)
                _state.update { it.copy(selectedWalk = null) }
            }
        }
    }

    private fun toUi(walk: Walk): WalkUi = WalkUi(
        id = walk.id,
        dateLabel = dateFormatter.format(walk.startTime.atZone(ZoneId.systemDefault())),
        distanceLabel = formatDistance(walk.distanceMiles),
        durationLabel = formatDuration(walk.durationSeconds),
        avgSpeedLabel = formatSpeed(walk.avgSpeedMph),
        maxSpeedLabel = formatSpeed(walk.maxSpeedMph),
        route = walk.route
    )
}
