package com.example.proximity.tv

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.proximity.shared.*
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.*

class AdvertiserService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val connectedEndpoints = mutableSetOf<String>()

    private val lifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Nearby.getConnectionsClient(this@AdvertiserService)
                .acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpoints.add(endpointId)
                TvState.connectedPhones.value = connectedEndpoints.toSet()
                sendHello(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)
            TvState.connectedPhones.value = connectedEndpoints.toSet()
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            when (val msg = bytes.decodePayload()) {
                is ProximityPayload.Ping -> handlePing(endpointId, msg)
                is ProximityPayload.RenderUi -> {
                    if (msg.root.type.equals("clear", ignoreCase = true)) {
                        TvState.remoteUi.value = null
                    } else {
                        TvState.remoteUi.value = msg.root
                    }
                }
                else -> Unit
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    override fun onCreate() {
        super.onCreate()
        postForegroundNotification()
        startAdvertising()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        Nearby.getConnectionsClient(this).stopAdvertising()
        Nearby.getConnectionsClient(this).stopAllEndpoints()
        TvState.connectedPhones.value = emptySet()
    }

    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        scope.launch {
            runCatching {
                Nearby.getConnectionsClient(this@AdvertiserService)
                    .awaitStartAdvertising(Build.MODEL, ServiceIds.NEARBY, options, lifecycleCallback)
            }
        }
    }

    private fun sendHello(endpointId: String) {
        val hello = ProximityPayload.Hello(device = Build.MODEL, model = Build.MANUFACTURER)
        Nearby.getConnectionsClient(this)
            .sendPayload(endpointId, Payload.fromBytes(hello.encode()))
    }

    private fun handlePing(endpointId: String, ping: ProximityPayload.Ping) {
        val pong = ProximityPayload.Pong(
            nonce = ping.nonce,
            tSent = ping.tSent,
            tEcho = System.currentTimeMillis(),
        )
        Nearby.getConnectionsClient(this)
            .sendPayload(endpointId, Payload.fromBytes(pong.encode()))
        TvState.lastPingFrom.value = endpointId
    }

    private fun postForegroundNotification() {
        val channelId = "proximity_advertiser"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ProximiTTY Advertiser",
                NotificationManager.IMPORTANCE_LOW,
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("ProximiTTY TV")
            .setContentText("Visible to nearby phones")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
