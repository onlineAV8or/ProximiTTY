package com.example.proximity.phone

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proximity.shared.*
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FoundEndpoint(val id: String, val name: String)

sealed class ConnectionState {
    object Idle : ConnectionState()
    data class Connecting(val endpoint: FoundEndpoint) : ConnectionState()
    data class Connected(val endpoint: FoundEndpoint, val lastRttMs: Long? = null) : ConnectionState()
    data class Failed(val message: String) : ConnectionState()
}

class DiscoveryViewModel : ViewModel() {

    private val _endpoints = MutableStateFlow<List<FoundEndpoint>>(emptyList())
    val endpoints: StateFlow<List<FoundEndpoint>> = _endpoints

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private var client: ConnectionsClient? = null
    private var connectedEndpointId: String? = null

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val ep = FoundEndpoint(endpointId, info.endpointName)
            _endpoints.update { list -> if (list.none { it.id == endpointId }) list + ep else list }
        }

        override fun onEndpointLost(endpointId: String) {
            _endpoints.update { it.filter { ep -> ep.id != endpointId } }
            if (connectedEndpointId == endpointId) {
                connectedEndpointId = null
                _connectionState.value = ConnectionState.Idle
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Both sides must accept for the connection to complete
            client?.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpointId = endpointId
                val ep = _endpoints.value.find { it.id == endpointId }
                    ?: FoundEndpoint(endpointId, "TV")
                _connectionState.value = ConnectionState.Connected(ep)
                sendHello()
            } else {
                _connectionState.value =
                    ConnectionState.Failed(result.status.statusMessage ?: "Connection failed")
            }
        }

        override fun onDisconnected(endpointId: String) {
            if (connectedEndpointId == endpointId) {
                connectedEndpointId = null
                _connectionState.value = ConnectionState.Idle
            }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            when (val msg = bytes.decodePayload()) {
                is ProximityPayload.Pong -> {
                    val rtt = System.currentTimeMillis() - msg.tSent
                    _connectionState.update { s ->
                        if (s is ConnectionState.Connected) s.copy(lastRttMs = rtt) else s
                    }
                }
                else -> Unit
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    fun startScanning(context: Context) {
        val c = Nearby.getConnectionsClient(context).also { client = it }
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        viewModelScope.launch {
            runCatching {
                c.awaitStartDiscovery(ServiceIds.NEARBY, options, endpointDiscoveryCallback)
                _isScanning.value = true
            }.onFailure {
                _connectionState.value = ConnectionState.Failed(it.message ?: "Discovery failed")
            }
        }
    }

    fun setPermissionError(message: String) {
        _connectionState.value = ConnectionState.Failed(message)
    }

    fun stopScanning() {
        client?.stopDiscovery()
        _isScanning.value = false
        _endpoints.value = emptyList()
    }

    fun connectTo(context: Context, endpoint: FoundEndpoint) {
        val c = client ?: Nearby.getConnectionsClient(context).also { client = it }
        _connectionState.value = ConnectionState.Connecting(endpoint)
        viewModelScope.launch {
            runCatching {
                c.awaitRequestConnection(Build.MODEL, endpoint.id, connectionLifecycleCallback)
            }.onFailure {
                _connectionState.value = ConnectionState.Failed(it.message ?: "Request failed")
            }
        }
    }

    fun disconnect() {
        connectedEndpointId?.let { client?.disconnectFromEndpoint(it) }
        connectedEndpointId = null
        _connectionState.value = ConnectionState.Idle
    }

    fun sendPing() {
        val id = connectedEndpointId ?: return
        val ping = ProximityPayload.Ping(nonce = System.nanoTime(), tSent = System.currentTimeMillis())
        client?.sendPayload(id, Payload.fromBytes(ping.encode()))
    }

    /** Send a UiNode tree to the TV. Returns null on success, error string on failure. */
    fun sendUi(node: UiNode): String? {
        val id = connectedEndpointId ?: return "Not connected"
        return runCatching {
            val payload = ProximityPayload.RenderUi(node)
            client?.sendPayload(id, Payload.fromBytes(payload.encode()))
        }.fold(onSuccess = { null }, onFailure = { it.message ?: "send failed" })
    }

    /** Parse JSON text into a UiNode and send. Returns null on success, error string on failure. */
    fun sendUiJson(json: String): String? {
        val node = parseUiNode(json) ?: return "Invalid JSON"
        if (node.type.isBlank()) return "Missing \"type\" field"
        return sendUi(node)
    }

    fun clearTvUi(): String? = sendUi(UiNode(type = "clear"))

    private fun sendHello() {
        val id = connectedEndpointId ?: return
        val hello = ProximityPayload.Hello(device = Build.MODEL, model = Build.MANUFACTURER)
        client?.sendPayload(id, Payload.fromBytes(hello.encode()))
    }

    override fun onCleared() {
        super.onCleared()
        client?.stopAllEndpoints()
        client?.stopDiscovery()
    }
}
