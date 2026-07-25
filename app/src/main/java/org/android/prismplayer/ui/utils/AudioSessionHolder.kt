package org.android.prismplayer.ui.utils

import kotlinx.coroutines.flow.MutableStateFlow

object AudioSessionHolder {
    private val _sessionId = MutableStateFlow(0)

    fun updateSessionId(id: Int) {
        _sessionId.value = id
    }
}