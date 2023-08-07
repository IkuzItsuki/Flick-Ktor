package com.ikuzMirel.data.user

import com.ikuzMirel.data.friends.Friend
import com.ikuzMirel.serializer.ObjectIdSerializer
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class User(
    val username: String,
    val email: String,
    val friends: List<Friend> = emptyList(),
    @Serializable(with = ObjectIdSerializer::class)
    @BsonId val id: ObjectId
)
