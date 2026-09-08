package com.golfpvcc.martytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.golfpvcc.martytracker.ui.about.AboutScreen
import com.golfpvcc.martytracker.ui.history.HistoryRoot
import com.golfpvcc.martytracker.ui.theme.MartyTrackerTheme
import com.golfpvcc.martytracker.ui.tracking.TrackingRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MartyTrackerTheme {
                MartyTrackerApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MartyTrackerApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.TRACK) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.TRACK -> TrackingRoot()
            AppDestinations.HISTORY -> HistoryRoot()
            AppDestinations.ABOUT -> AboutScreen()
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    TRACK("Track", R.drawable.ic_home),
    HISTORY("History", R.drawable.ic_favorite),
    ABOUT("About", R.drawable.ic_account_box),
}
