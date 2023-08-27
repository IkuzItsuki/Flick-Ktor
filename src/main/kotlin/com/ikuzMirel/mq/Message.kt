package com.ikuzMirel.mq


sealed class Message {
    data class ChatMessage(
        val userId: String,
        val receiverId: String,
        val data: String
    ) : Message()

    data class FriendRequestMessage(
        val targetUserId: String,
        val data: String
    ) : Message()
}