package com.ikuzMirel.data.user

import com.ikuzMirel.data.friends.Friend
import com.ikuzMirel.serializer.ObjectIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class User(
    val username: String,
    val email: String,
    val friends: List<Friend> = emptyList(),
    @SerialName("id")
    @Serializable(ObjectIdSerializer::class)
    val _id: ObjectId
)
