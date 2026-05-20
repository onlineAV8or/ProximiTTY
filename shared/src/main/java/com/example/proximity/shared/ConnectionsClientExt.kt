package com.example.proximity.shared

import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun ConnectionsClient.awaitStartAdvertising(
    name: String,
    serviceId: String,
    options: AdvertisingOptions,
    lifecycleCallback: ConnectionLifecycleCallback,
) = suspendCancellableCoroutine { cont ->
    startAdvertising(name, serviceId, lifecycleCallback, options)
        .addOnSuccessListener { cont.resume(Unit) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

suspend fun ConnectionsClient.awaitStartDiscovery(
    serviceId: String,
    options: DiscoveryOptions,
    endpointCallback: EndpointDiscoveryCallback,
) = suspendCancellableCoroutine { cont ->
    startDiscovery(serviceId, endpointCallback, options)
        .addOnSuccessListener { cont.resume(Unit) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

suspend fun ConnectionsClient.awaitRequestConnection(
    name: String,
    endpointId: String,
    lifecycleCallback: ConnectionLifecycleCallback,
) = suspendCancellableCoroutine { cont ->
    requestConnection(name, endpointId, lifecycleCallback)
        .addOnSuccessListener { cont.resume(Unit) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

suspend fun ConnectionsClient.awaitAcceptConnection(
    endpointId: String,
    payloadCallback: PayloadCallback,
) = suspendCancellableCoroutine { cont ->
    acceptConnection(endpointId, payloadCallback)
        .addOnSuccessListener { cont.resume(Unit) }
        .addOnFailureListener { cont.resumeWithException(it) }
}
