package com.ikuzMirel.data.user

import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.regex

class UserDataSourceImpl(
    db : CoroutineDatabase
): UserDataSource {
    private val users = db.getCollection<User>()
    override suspend fun getUserById(id: String): User? {
        val objectId = ObjectId(id)
        return users.findOne(User::id eq objectId)
    }

    override suspend fun getUsersByName(username: String): List<User> {
        return users.find(User::username regex username).toList()
    }

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }
}