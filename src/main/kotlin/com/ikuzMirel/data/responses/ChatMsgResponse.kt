package com.ikuzMirel.data.responses

import com.ikuzMirel.data.message.MessageWithCid
import kotlinx.serialization.Serializable

@Serializable
data class ChatMsgResponse(
    val messages: List<MessageWithCid>
)
