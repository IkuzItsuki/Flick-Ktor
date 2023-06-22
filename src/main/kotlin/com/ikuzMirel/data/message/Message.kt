package com.ikuzMirel.data.message

import com.ikuzMirel.serializer.ObjectIdSerializer
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Message(
    val content: String,
    val senderUid: String,
    val timestamp: Long,
    @Serializable(with = ObjectIdSerializer::class)
    @BsonId val id: ObjectId = ObjectId()
)
