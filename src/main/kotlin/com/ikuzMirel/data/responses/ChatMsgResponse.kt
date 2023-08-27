package com.ikuzMirel.data.responses

import com.ikuzMirel.data.chatMessage.ChatMessageWithCid
import kotlinx.serialization.Serializable

@Serializable
data class ChatMsgResponse(
    val messages: List<ChatMessageWithCid>
)
