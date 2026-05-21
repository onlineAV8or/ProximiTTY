package com.example.proximitty.tv

import android.net.Uri
import com.example.proximitty.shared.UiNode
import kotlinx.coroutines.flow.MutableStateFlow

object TvState {
    val connectedPhones = MutableStateFlow<Set<String>>(emptySet())
    val lastPingFrom = MutableStateFlow<String?>(null)
    val remoteUi = MutableStateFlow<UiNode?>(null)
    /** Most recently received photo as a content URI. null = no photo. */
    val currentImage = MutableStateFlow<Uri?>(null)
}
