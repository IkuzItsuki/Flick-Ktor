package com.ikuzMirel.data.user

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoUserDataSouce(
    db: CoroutineDatabase
): UserDataSouce {
    private val users = db.getCollection<User>()
    override suspend fun getUserByUsername(username: String): User? {
        return users.findOne(User::username eq username)
    }

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }
}