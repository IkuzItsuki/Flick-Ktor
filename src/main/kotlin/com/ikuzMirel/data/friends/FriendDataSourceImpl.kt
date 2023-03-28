package com.ikuzMirel.data.friends

import com.ikuzMirel.data.user.User
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class FriendDataSourceImpl(
    db: CoroutineDatabase
): FriendDataSource {

    private val userFriends = db.getCollection<User>()
    override suspend fun getAllFriends(userId: String): List<Friend> {
        val userObjID = ObjectId(userId)
        val user = userFriends.findOne(User::id eq userObjID)
        return user?.friends ?: emptyList()
    }

    override suspend fun deleteFriend(userId: String, friend: Friend): Boolean {
        val userObjID = ObjectId(userId)
        val user = userFriends.findOne(User::id eq userObjID)
        return user?.copy(friends = user.friends - friend)
            ?.let { userFriends.updateOne(User::id eq userObjID, it).wasAcknowledged() } ?: false
    }

    override suspend fun addFriend(userId: String, friend: Friend): Boolean {
        val userObjID = ObjectId(userId)
        val user = userFriends.findOne(User::id eq userObjID)
        return user?.copy(friends = user.friends + friend)
            ?.let { userFriends.updateOne(User::id eq userObjID, it).wasAcknowledged() } ?: false
    }
}