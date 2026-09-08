package com.golfpvcc.martytracker.ui.tracking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.golfpvcc.martytracker.ui.formatDistance
import com.golfpvcc.martytracker.ui.formatDuration
import com.golfpvcc.martytracker.ui.formatSpeed

@Composable
fun WalkSummaryDialog(
    state: TrackingState,
    onAction: (TrackingAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Walk complete") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Distance: ${formatDistance(state.distanceMiles)}")
                Text("Duration: ${formatDuration(state.elapsedSeconds)}")
                Text("Average speed: ${formatSpeed(state.avgSpeedMph)}")
                Text("Max speed: ${formatSpeed(state.maxSpeedMph)}")
            }
        },
        confirmButton = {
            TextButton(onClick = { onAction(TrackingAction.OnSaveWalkClick) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(TrackingAction.OnDiscardWalkClick) }) {
                Text("Discard")
            }
        }
    )
}
