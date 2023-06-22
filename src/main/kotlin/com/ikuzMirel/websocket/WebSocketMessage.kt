package com.ikuzMirel.websocket

import com.ikuzMirel.serializer.WebSocketMsgSerializer
import kotlinx.serialization.Serializable

@Serializable(with = WebSocketMsgSerializer::class)
data class WebSocketMessage(
    val type: String,
    val data: Any
)
