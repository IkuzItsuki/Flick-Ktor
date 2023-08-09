package com.ikuzMirel.data.auth

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId


class AuthSourceImpl(
    db: MongoDatabase
): AuthSource {
    private val auths = db.getCollection<Auth>("auth")
    override suspend fun getAuthByUsername(username: String): Auth? {
        return auths.find(Filters.eq(Auth::username.name, username)).firstOrNull()
    }

    override suspend fun getAuthByUserId(id: String): Auth? {
        val objectId = ObjectId(id)
        return auths.find(Filters.eq(Auth::_id.name, objectId)).firstOrNull()
    }

    override suspend fun insertAuth(auth: Auth): Boolean {
        return auths.insertOne(auth).wasAcknowledged()
    }
}