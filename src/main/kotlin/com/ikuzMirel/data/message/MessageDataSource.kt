package com.ikuzMirel.data.message

interface MessageDataSource {

    suspend fun getAllMessages(collectionId: String): List<Message>
    suspend fun insertMessage(collectionId: String, message: Message): Boolean
    suspend fun createMessageCollection(): String
}