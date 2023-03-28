package com.ikuzMirel.data.message

import org.bson.types.ObjectId
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
        db.createCollection(newId.toString())
        return newId.toString()
    }

}