package com.golfpvcc.martytracker.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.golfpvcc.martytracker.ui.common.RoutePolylineMap
import com.golfpvcc.martytracker.ui.history.model.WalkUi
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryRoot(viewModel: HistoryViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HistoryScreen(state = state, onAction = viewModel::onAction)
}

@Composable
private fun HistoryScreen(
    state: HistoryState,
    onAction: (HistoryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.walks.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No walks saved yet")
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.walks, key = { it.id }) { walk ->
                        WalkRow(walk = walk, onClick = { onAction(HistoryAction.OnWalkClick(walk.id)) })
                    }
                }
            }
        }

        state.selectedWalk?.let { walk ->
            WalkDetailDialog(
                walk = walk,
                onClose = { onAction(HistoryAction.OnDismissDetail) },
                onDelete = { onAction(HistoryAction.OnDeleteWalk(walk.id)) }
            )
        }
    }
}

@Composable
private fun WalkRow(walk: WalkUi, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(walk.dateLabel)
            Text("${walk.distanceLabel} • ${walk.durationLabel} • ${walk.avgSpeedLabel} avg")
        }
    }
}

@Composable
private fun WalkDetailDialog(
    walk: WalkUi,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            RoutePolylineMap(
                routePoints = walk.route,
                modifier = Modifier.fillMaxSize()
            )
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(walk.dateLabel, style = MaterialTheme.typography.titleMedium)
                    Text("Distance: ${walk.distanceLabel}")
                    Text("Duration: ${walk.durationLabel}")
                    Text("Average speed: ${walk.avgSpeedLabel}")
                    Text("Max speed: ${walk.maxSpeedLabel}")
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onDelete, modifier = Modifier.align(Alignment.CenterStart)) {
                            Text("Delete")
                        }
                        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd)) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
