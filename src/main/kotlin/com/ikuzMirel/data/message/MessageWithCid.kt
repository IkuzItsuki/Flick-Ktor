package com.ikuzMirel.data.message

import kotlinx.serialization.Serializable

// Created because of the need to send the cid to the client but not to the database
@Serializable
data class MessageWithCid(
    val id: String,
    val content: String,
    val senderUid: String,
    val timestamp: Long,
    val collectionId: String
)
