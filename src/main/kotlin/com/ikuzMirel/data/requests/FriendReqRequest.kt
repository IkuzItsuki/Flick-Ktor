package com.ikuzMirel.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class FriendReqRequest(
    val senderId: String,
    val receiverId: String
)
