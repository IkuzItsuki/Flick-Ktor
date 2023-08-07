package com.ikuzMirel.data.message

import com.mongodb.client.model.ClusteredIndexOptions
import com.mongodb.client.model.CreateCollectionOptions
import org.bson.types.ObjectId
import org.litote.kmongo.bson
import org.litote.kmongo.coroutine.CoroutineDatabase

class MessageDataSourceImpl(
    private val db: CoroutineDatabase
): MessageDataSource {

    private fun getCollection(collectionId: String) = db.getCollection<Message>(collectionId)

    override suspend fun getAllMessages(collectionId: String): List<Message> {
        val messages = getCollection(collectionId)
        return messages.find()
            .descendingSort(Message::timestamp)
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
                ClusteredIndexOptions("""{ _id: 1 }""".bson, true)
            )
        )
        return newId.toString()
    }

}