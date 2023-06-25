package com.ikuzMirel.data.user

import kotlinx.serialization.Serializable

@Serializable
data class UserSearchResult(
    val userId: String,
    val username: String,
    val friendWithMe: Boolean
)
