package com.ikuzMirel.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val username: String,
    val userId: String
)