package com.ikuzMirel.data.message

import com.ikuzMirel.serializer.ObjectIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Message(
    val content: String,
    val senderUid: String,
    val timestamp: Long,
    @SerialName("id")
    @Serializable(ObjectIdSerializer::class)
    val _id: ObjectId
)
