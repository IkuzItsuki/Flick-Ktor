package com.ikuzMirel.data.friends

import com.ikuzMirel.serializer.ObjectIdSerializer
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class FriendRequest(
    val senderId: String,
    val receiverId: String,
    val status: String,
    @Serializable(with = ObjectIdSerializer::class)
    @BsonId val id: ObjectId = ObjectId()
)
