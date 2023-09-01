package com.ikuzMirel.data.chatMessage

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageRequest(
    val message: String,
    val collectionId: String,
    val receiverId: String,
    val id: String,
)
