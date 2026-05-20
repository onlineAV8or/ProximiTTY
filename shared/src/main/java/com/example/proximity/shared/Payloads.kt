package com.example.proximity.shared

import com.google.gson.Gson

private val gson = Gson()

sealed class ProximityPayload {
    data class Hello(val device: String, val model: String) : ProximityPayload()
    data class Ping(val nonce: Long, val tSent: Long) : ProximityPayload()
    data class Pong(val nonce: Long, val tSent: Long, val tEcho: Long) : ProximityPayload()
    data class RenderUi(val root: UiNode) : ProximityPayload()
}

private data class Envelope(val kind: String, val data: String)

fun ProximityPayload.encode(): ByteArray {
    val (kind, data) = when (this) {
        is ProximityPayload.Hello    -> "hello"    to gson.toJson(this)
        is ProximityPayload.Ping     -> "ping"     to gson.toJson(this)
        is ProximityPayload.Pong     -> "pong"     to gson.toJson(this)
        is ProximityPayload.RenderUi -> "renderUi" to gson.toJson(this)
    }
    return gson.toJson(Envelope(kind, data)).toByteArray(Charsets.UTF_8)
}

fun ByteArray.decodePayload(): ProximityPayload? = runCatching {
    val env = gson.fromJson(toString(Charsets.UTF_8), Envelope::class.java)
    when (env.kind) {
        "hello"    -> gson.fromJson(env.data, ProximityPayload.Hello::class.java)
        "ping"     -> gson.fromJson(env.data, ProximityPayload.Ping::class.java)
        "pong"     -> gson.fromJson(env.data, ProximityPayload.Pong::class.java)
        "renderUi" -> gson.fromJson(env.data, ProximityPayload.RenderUi::class.java)
        else       -> null
    }
}.getOrNull()

/** Convenience for parsing a raw JSON UiNode string outside of payloads. */
fun parseUiNode(json: String): UiNode? = runCatching {
    gson.fromJson(json, UiNode::class.java)
}.getOrNull()
