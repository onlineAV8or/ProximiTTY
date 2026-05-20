package com.example.proximity.phone

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.proximity.phone.screens.ConnectedScreen
import com.example.proximity.phone.screens.ScanScreen
import com.example.proximity.phone.ui.theme.ProximityTheme
import com.example.proximity.shared.NEARBY_PERMISSIONS

class MainActivity : ComponentActivity() {

    private val vm: DiscoveryViewModel by viewModels()

    // ── Immersive auto-hide ──────────────────────────────────────────────────
    private val autoHideDelayMs = 3_500L
    private val handler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hideNavBar() }
    private lateinit var insetsController: WindowInsetsControllerCompat

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val denied = results.filterValues { !it }.keys
        when {
            denied.isEmpty() -> vm.startScanning(this)
            denied.any { !shouldShowRequestPermissionRationale(it) } ->
                vm.setPermissionError(
                    "Location & Bluetooth permissions permanently denied.\n" +
                    "Go to Settings → Apps → ProximiTTY → Permissions and allow all."
                )
            else ->
                vm.setPermissionError("Permissions denied: ${denied.joinToString { it.substringAfterLast('.') }}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up immersive controller. Nav bar reappears via swipe-from-edge.
        insetsController = WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            ProximityTheme {
                val state by vm.connectionState.collectAsState()
                val endpoints by vm.endpoints.collectAsState()
                val scanning by vm.isScanning.collectAsState()

                when (val s = state) {
                    is ConnectionState.Connected -> ConnectedScreen(
                        state = s,
                        onPing = vm::sendPing,
                        onDisconnect = vm::disconnect,
                        onSendTemplate = { idx ->
                            vm.sendUi(UiTemplates.ALL[idx].node)
                        },
                        onSendCustomJson = { json -> vm.sendUiJson(json) },
                        onClearTvUi = { vm.clearTvUi() },
                    )
                    else -> ScanScreen(
                        endpoints = endpoints,
                        isScanning = scanning,
                        connectionState = state,
                        onStartScan = { requestOrScan() },
                        onStopScan = vm::stopScanning,
                        onConnect = { vm.connectTo(this, it) },
                        onOpenSettings = { openAppSettings() },
                    )
                }
            }
        }
    }

    private fun requestOrScan() {
        val missing = NEARBY_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            vm.startScanning(this)
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun openAppSettings() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        )
    }

    // ── Auto-hide logic ──────────────────────────────────────────────────────
    // Initial show with a 3.5s timer, then the bar disappears. After that the
    // ONLY way it returns is a transient swipe from the bottom edge of the
    // screen (handled automatically by BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE).

    private fun hideNavBar() {
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }

    private fun scheduleHide() {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, autoHideDelayMs)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // After returning from a dialog/permission prompt the OS will re-show
        // the bar; re-arm the timer so it goes away again.
        if (hasFocus) scheduleHide()
    }

    override fun onResume() {
        super.onResume()
        scheduleHide()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(hideRunnable)
        vm.disconnect()
        vm.stopScanning()
    }
}
