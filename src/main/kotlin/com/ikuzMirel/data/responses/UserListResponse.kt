package com.ikuzMirel.data.responses

import com.ikuzMirel.data.user.UserSearchResult
import kotlinx.serialization.Serializable

@Serializable
data class UserListResponse(
    val users: List<UserSearchResult>
)