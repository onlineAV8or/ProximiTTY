package com.example.proximity.tv

import com.example.proximity.shared.UiNode
import kotlinx.coroutines.flow.MutableStateFlow

object TvState {
    val connectedPhones = MutableStateFlow<Set<String>>(emptySet())
    val lastPingFrom = MutableStateFlow<String?>(null)
    val remoteUi = MutableStateFlow<UiNode?>(null)
}
