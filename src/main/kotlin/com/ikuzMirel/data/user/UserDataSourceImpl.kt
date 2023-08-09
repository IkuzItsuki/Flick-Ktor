package com.ikuzMirel.data.user

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class UserDataSourceImpl(
    db: MongoDatabase
): UserDataSource {
    private val users = db.getCollection<User>("user")
    override suspend fun getUserById(id: String): User? {
        val objectId = ObjectId(id)
        return users.find(Filters.eq(User::_id.name, objectId)).firstOrNull()
    }

    override suspend fun getUsersByName(username: String): List<User> {
        return users.find(Filters.regex(User::username.name, username)).toList()
    }

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }
}