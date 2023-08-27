package com.ikuzMirel.data.chatMessage

import com.mongodb.client.model.ClusteredIndexOptions
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId

class ChatMessageDataSourceImpl(
    private val db: MongoDatabase
) : ChatMessageDataSource {

    private fun getCollection(collectionId: String) = db.getCollection<ChatMessage>(collectionId)

    override suspend fun getAllMessages(collectionId: String): List<ChatMessage> {
        val messages = getCollection(collectionId)
        return messages.find()
            .sort(Sorts.descending(ChatMessage::timestamp.name))
            .toList()
    }

    override suspend fun insertMessage(collectionId: String, chatMessage: ChatMessage): Boolean {
        val messages = getCollection(collectionId)
        return messages.insertOne(chatMessage).wasAcknowledged()
    }

    override suspend fun createMessageCollection(): String {
        val newId = ObjectId()
        db.createCollection(
            newId.toString(), CreateCollectionOptions().clusteredIndexOptions(
                ClusteredIndexOptions(Document("_id", 1), true)
            )
        )
        return newId.toString()
    }

}