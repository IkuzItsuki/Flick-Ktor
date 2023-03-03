package com.ikuzMirel.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class InfoResponse (
    val username: String,
    val email: String
)