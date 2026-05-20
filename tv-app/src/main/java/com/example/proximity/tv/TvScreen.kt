package com.example.proximity.tv

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TvScreen() {
    val connectedPhones by TvState.connectedPhones.collectAsState()
    val pingFrom by TvState.lastPingFrom.collectAsState()
    val remoteUi by TvState.remoteUi.collectAsState()

    var showOverlay by remember { mutableStateOf(false) }
    var lastPinger by remember { mutableStateOf("") }

    LaunchedEffect(pingFrom) {
        if (pingFrom != null) {
            lastPinger = pingFrom!!
            showOverlay = true
            delay(2_000)
            showOverlay = false
            TvState.lastPingFrom.value = null
        }
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // ── Foreground: either remote UI or default status ─────────────
            if (remoteUi != null) {
                RenderUi(remoteUi!!, modifier = Modifier.fillMaxSize())
            } else {
                DefaultStatus(connectedCount = connectedPhones.size)
            }

            // ── Ping overlay flashes briefly when a phone sends a ping ─────
            AnimatedVisibility(
                visible = showOverlay,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PING!",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("from $lastPinger",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultStatus(connectedCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("ProximiTTY", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            "Advertising via Nearby Connections",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        if (connectedCount == 0) {
            Text("Waiting for phones…", style = MaterialTheme.typography.titleLarge)
        } else {
            Text(
                "$connectedCount phone(s) connected",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
