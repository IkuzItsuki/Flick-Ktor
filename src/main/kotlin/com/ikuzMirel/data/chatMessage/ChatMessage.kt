package com.ikuzMirel.data.chatMessage

import com.ikuzMirel.serializer.ObjectIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class ChatMessage(
    val content: String,
    val senderUid: String,
    val timestamp: Long,
    @SerialName("id")
    @Serializable(ObjectIdSerializer::class)
    val _id: ObjectId
)
