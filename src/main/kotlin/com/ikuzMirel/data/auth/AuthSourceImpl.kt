package com.ikuzMirel.data.auth

import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class AuthSourceImpl(
    db: CoroutineDatabase
): AuthSource {
    private val auths = db.getCollection<Auth>()
    override suspend fun getAuthByUsername(username: String): Auth? {
        return auths.findOne(Auth::username eq username)
    }

    override suspend fun getAuthByUserId(id: String): Auth? {
        val objectId = ObjectId(id)
        return auths.findOne(Auth::id eq objectId)
    }

    override suspend fun insertAuth(auth: Auth): Boolean {
        return auths.insertOne(auth).wasAcknowledged()
    }
}