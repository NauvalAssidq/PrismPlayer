package org.android.prismplayer.ui.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AudioSessionHolder {
    private val _sessionId = MutableStateFlow(0)
    val sessionId = _sessionId.asStateFlow()

    fun updateSessionId(id: Int) {
        _sessionId.value = id
    }
}