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

fun ChatMessage.toChatMessageWithCid(collectionId: String): ChatMessageWithCid {
    return ChatMessageWithCid(
        content = content,
        senderUid = senderUid,
        timestamp = timestamp,
        id = _id.toString(),
        collectionId = collectionId
    )
}