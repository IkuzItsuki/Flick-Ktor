package com.ikuzMirel.data.user

import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoUserDataSource(
    db: CoroutineDatabase
): UserDataSource {
    private val users = db.getCollection<User>()
    override suspend fun getUserByUsername(username: String): User? {
        return users.findOne(User::username eq username)
    }

    override suspend fun getUserByUserId(id: String): User? {
        val objectId = ObjectId(id)
        return users.findOne(User::id eq objectId)
    }

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }
}