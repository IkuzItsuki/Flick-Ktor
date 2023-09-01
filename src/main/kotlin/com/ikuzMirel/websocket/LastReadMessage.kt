package com.ikuzMirel.websocket

import kotlinx.serialization.Serializable

@Serializable
data class LastReadMessage(
    val friendUserId: String,
    val lastReadMessageTime: Long
)

@Serializable
data class LastReadMessageSet(
    val lastReadMessageList: List<LastReadMessage>
)