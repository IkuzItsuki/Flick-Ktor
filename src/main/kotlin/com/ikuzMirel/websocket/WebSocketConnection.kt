package com.ikuzMirel.websocket

import io.ktor.websocket.*

data class WebSocketConnection(
    val uid: String,
    val username: String,
    val sessionId: String,
    val socket: WebSocketSession
)
