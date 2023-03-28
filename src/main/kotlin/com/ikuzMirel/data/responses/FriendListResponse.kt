package com.ikuzMirel.data.responses

import com.ikuzMirel.data.friends.Friend
import kotlinx.serialization.Serializable

@Serializable
data class FriendListResponse(
    val friends: List<Friend>
)
