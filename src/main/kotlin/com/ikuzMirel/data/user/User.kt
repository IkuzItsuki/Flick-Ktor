package com.ikuzMirel.data.user

import com.ikuzMirel.data.friends.Friend
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val username: String,
    val email: String,
    val friends: List<Friend> = emptyList(),
    @BsonId val id: ObjectId
)
