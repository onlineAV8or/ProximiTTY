package com.example.proximitty.tv

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

@Composable
fun TvScreen() {
    val connectedPhones by TvState.connectedPhones.collectAsState()
    val pingFrom by TvState.lastPingFrom.collectAsState()
    val remoteUi by TvState.remoteUi.collectAsState()
    val currentImage by TvState.currentImage.collectAsState()
    val context = LocalContext.current

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

            when {
                currentImage != null -> {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentImage)
                            .listener(
                                onStart = { Log.d("ProximiTTY/TV", "Coil start: ${it.data}") },
                                onSuccess = { _, r -> Log.d("ProximiTTY/TV", "Coil success: ${r.dataSource}") },
                                onError = { _, r -> Log.e("ProximiTTY/TV", "Coil ERROR: ${r.throwable.message}", r.throwable) },
                            )
                            .build(),
                        contentDescription = "Photo from phone",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(Modifier.height(20.dp))
                                    Text("Loading image…", style = MaterialTheme.typography.titleLarge)
                                    Spacer(Modifier.height(8.dp))
                                    Text("$currentImage",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⚠  Image load failed",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.height(20.dp))
                                    Text("URI:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$currentImage",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(12.dp))
                                    Text("Reason:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${it.result.throwable.message ?: it.result.throwable::class.simpleName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(20.dp))
                                    Text("(adb logcat -s ProximiTTY/TV:V for details)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                    )
                }
                remoteUi != null -> RenderUi(remoteUi!!, modifier = Modifier.fillMaxSize())
                else -> DefaultStatus(connectedCount = connectedPhones.size)
            }

            // ── Ping overlay ──
            AnimatedVisibility(visible = showOverlay, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PING!", style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("from $lastPinger", style = MaterialTheme.typography.titleLarge,
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
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("ProximiTTY", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text("Advertising via Nearby Connections",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        if (connectedCount == 0) {
            Text("Waiting for phones…", style = MaterialTheme.typography.titleLarge)
        } else {
            Text("$connectedCount phone(s) connected", style = MaterialTheme.typography.titleLarge)
        }
    }
}
