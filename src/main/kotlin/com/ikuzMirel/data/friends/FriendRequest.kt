package com.ikuzMirel.data.friends

import com.ikuzMirel.data.user.User
import com.ikuzMirel.serializer.ObjectIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

@Serializable
data class FriendRequest(
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val receiverName: String,
    val status: String,
    @SerialName("id")
    @Serializable(ObjectIdSerializer::class)
    val _id: ObjectId = ObjectId()
)

@Serializable
data class FriendRequestWithAggregation(
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val receiverName: String,
    val status: String,
    val sender: User,
    val receiver: User,
    @SerialName("id")
    @Serializable(ObjectIdSerializer::class)
    val _id: ObjectId,
)