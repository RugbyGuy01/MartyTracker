package com.golfpvcc.martytracker.ui.tracking

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.golfpvcc.martytracker.domain.model.TrackingStatus
import com.golfpvcc.martytracker.ui.common.RoutePolylineMap
import com.golfpvcc.martytracker.ui.formatDistance
import com.golfpvcc.martytracker.ui.formatDuration
import com.golfpvcc.martytracker.ui.formatSpeed
import com.golfpvcc.martytracker.ui.permissions.hasBackgroundLocationPermission
import com.golfpvcc.martytracker.ui.permissions.hasForegroundLocationPermission
import com.golfpvcc.martytracker.ui.permissions.hasNotificationPermission
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackingRoot(viewModel: TrackingViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showBackgroundRationale by remember { mutableStateOf(false) }
    var showLimitedBanner by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(hasForegroundLocationPermission(context)) }

    fun continueAfterForegroundGranted() {
        if (!hasBackgroundLocationPermission(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showBackgroundRationale = true
        } else {
            viewModel.onAction(TrackingAction.OnStartClick)
        }
    }

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) showLimitedBanner = true
        viewModel.onAction(TrackingAction.OnStartClick)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (!hasBackgroundLocationPermission(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showBackgroundRationale = true
        } else {
            viewModel.onAction(TrackingAction.OnStartClick)
        }
    }

    val foregroundPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        if (!granted) {
            scope.launch { snackbarHostState.showSnackbar("Location permission is required to track a walk.") }
        } else {
            hasLocationPermission = true
            if (!hasNotificationPermission(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                continueAfterForegroundGranted()
            }
        }
    }

    val startupLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) {
            hasLocationPermission = true
            viewModel.onLocationPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasForegroundLocationPermission(context)) {
            startupLocationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                TrackingEvent.WalkSaved -> snackbarHostState.showSnackbar("Walk saved")
            }
        }
    }

    fun onStartRequested() {
        if (hasForegroundLocationPermission(context)) {
            if (!hasNotificationPermission(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                continueAfterForegroundGranted()
            }
        } else {
            foregroundPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    if (showBackgroundRationale) {
        AlertDialog(
            onDismissRequest = { showBackgroundRationale = false },
            title = { Text("Track in the background?") },
            text = { Text("Allow \"All the time\" location access so your walk keeps recording if your screen locks or you switch apps.") },
            confirmButton = {
                TextButton(onClick = {
                    showBackgroundRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackgroundRationale = false
                    showLimitedBanner = true
                    viewModel.onAction(TrackingAction.OnStartClick)
                }) { Text("Not now") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        TrackingScreen(
            state = state,
            showLimitedBanner = showLimitedBanner,
            hasLocationPermission = hasLocationPermission,
            onOpenSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", context.packageName, null))
                )
            },
            onStartRequested = ::onStartRequested,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun TrackingScreen(
    state: TrackingState,
    showLimitedBanner: Boolean,
    hasLocationPermission: Boolean,
    onOpenSettings: () -> Unit,
    onStartRequested: () -> Unit,
    onAction: (TrackingAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (hasLocationPermission) {
            RoutePolylineMap(
                routePoints = state.routePoints,
                startupLocation = state.startupLocation,
                followCurrentLocation = state.status == TrackingStatus.TRACKING,
                modifier = Modifier.fillMaxWidth().weight(2f)
            )
        } else {
            Card(modifier = Modifier.fillMaxWidth().weight(2f)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Allow location access to see the map.")
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        if (showLimitedBanner) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Background tracking is limited. Your walk may stop recording if you leave the app.")
                    TextButton(onClick = onOpenSettings) { Text("Open Settings") }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Distance: ${formatDistance(state.distanceMiles)}")
                    Text("Duration: ${formatDuration(state.elapsedSeconds)}")
                }
                Text("Current speed: ${formatSpeed(state.currentSpeedMph)}")
                Text("Average speed: ${formatSpeed(state.avgSpeedMph)}")
                Text("Highest speed: ${formatSpeed(state.maxSpeedMph)}")
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (state.status) {
                TrackingStatus.IDLE, TrackingStatus.STOPPED -> {
                    Button(onClick = onStartRequested, modifier = Modifier.fillMaxWidth()) {
                        Text("Start")
                    }
                }
                TrackingStatus.TRACKING -> {
                    Button(onClick = { onAction(TrackingAction.OnPauseClick) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Pause")
                    }
                    Button(onClick = { onAction(TrackingAction.OnStopClick) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Stop")
                    }
                }
                TrackingStatus.PAUSED -> {
                    Button(onClick = { onAction(TrackingAction.OnResumeClick) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Resume")
                    }
                    Button(onClick = { onAction(TrackingAction.OnStopClick) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Stop")
                    }
                }
            }
        }

        if (state.showSummaryDialog) {
            WalkSummaryDialog(state = state, onAction = onAction)
        }
        }
    }
}
