package com.ikuzMirel.data.friends

import com.ikuzMirel.serializer.ObjectIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Friend(
    val username: String,
    @SerialName("_id")
    @Serializable(ObjectIdSerializer::class)
    val _id: ObjectId,
    val collectionId: String
)