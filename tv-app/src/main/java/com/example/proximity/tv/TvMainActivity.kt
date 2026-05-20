package com.example.proximity.tv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import com.example.proximity.shared.NEARBY_PERMISSIONS

class TvMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions(NEARBY_PERMISSIONS.toTypedArray(), 0)
        ContextCompat.startForegroundService(this, Intent(this, AdvertiserService::class.java))
        setContent {
            TvScreen()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, AdvertiserService::class.java))
    }
}
