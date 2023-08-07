package com.ikuzMirel.data.responses

import com.ikuzMirel.data.friends.FriendRequest
import kotlinx.serialization.Serializable

@Serializable
data class FriendReqResponse(
    val friendRequests: List<FriendRequest>
)