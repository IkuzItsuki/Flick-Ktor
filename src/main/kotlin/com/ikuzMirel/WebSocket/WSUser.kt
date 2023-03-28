package com.ikuzMirel.WebSocket

import io.ktor.websocket.*

data class WSUser(
    val uid: String,
    val username: String,
    val sessionId: String,
    val socket: WebSocketSession
)
