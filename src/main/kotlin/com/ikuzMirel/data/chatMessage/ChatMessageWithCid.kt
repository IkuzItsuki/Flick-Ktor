package com.ikuzMirel.data.chatMessage

import kotlinx.serialization.Serializable

// Created because of the need to send the cid to the client but not to the database
@Serializable
data class ChatMessageWithCid(
    val id: String,
    val content: String,
    val senderUid: String,
    val timestamp: Long,
    val collectionId: String
)
