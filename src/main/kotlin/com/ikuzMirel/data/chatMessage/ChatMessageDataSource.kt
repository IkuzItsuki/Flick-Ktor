package com.ikuzMirel.data.chatMessage

interface ChatMessageDataSource {

    suspend fun getAllMessages(collectionId: String): List<ChatMessage>
    suspend fun insertMessage(collectionId: String, chatMessage: ChatMessage): Boolean
    suspend fun createMessageCollection(): String
}