package com.ikuzMirel.data.message

import com.mongodb.client.model.ClusteredIndexOptions
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId

class MessageDataSourceImpl(
    private val db: MongoDatabase
): MessageDataSource {

    private fun getCollection(collectionId: String) = db.getCollection<Message>(collectionId)

    override suspend fun getAllMessages(collectionId: String): List<Message> {
        val messages = getCollection(collectionId)
        return messages.find()
            .sort(Sorts.descending(Message::timestamp.name))
            .toList()
    }

    override suspend fun insertMessage(collectionId: String, message: Message) : Boolean{
        val messages = getCollection(collectionId)
        return messages.insertOne(message).wasAcknowledged()
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