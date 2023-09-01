package com.ikuzMirel.data.friends

import com.ikuzMirel.data.user.User
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId

class FriendDataSourceImpl(
    db: MongoDatabase
): FriendDataSource {

    private val userFriends = db.getCollection<User>("user")
    override suspend fun getAllFriends(userId: String): List<Friend> {
        val userObjID = ObjectId(userId)
        val user = userFriends.find(Filters.eq(User::_id.name, userObjID)).firstOrNull()
        return user?.friends ?: emptyList()
    }

    override suspend fun getFriendById(userId: String, friendId: String): Friend? {
        val userObjID = ObjectId(userId)
        val friendObjID = ObjectId(friendId)
        val user = userFriends.find(Filters.eq(User::_id.name, userObjID)).firstOrNull() ?: return null
        return user.friends.find { it._id == friendObjID }
    }

    override suspend fun deleteFriend(userId: String, friend: Friend): Boolean {
        val userObjID = ObjectId(userId)
        val user = userFriends.find(Filters.eq(User::_id.name, userObjID)).firstOrNull()
        return user?.copy(friends = user.friends - friend)?.let {
            userFriends.updateOne(Filters.eq(User::_id.name, userObjID), Updates.set(User::friends.name, it.friends))
                .wasAcknowledged()
        } ?: false
    }

    override suspend fun addFriend(userId: String, friend: Friend): Boolean {
        val userObjID = ObjectId(userId)
        val user = userFriends.find(Filters.eq(User::_id.name, userObjID)).firstOrNull()
        return user?.copy(friends = user.friends + friend)?.let {
            userFriends.updateOne(Filters.eq(User::_id.name, userObjID), Updates.set(User::friends.name, it.friends))
                .wasAcknowledged()
        } ?: false
    }

    override suspend fun updateLastReadMessageTime(userId: String, friendId: String, timestamp: Long): Boolean {
        val userObjID = ObjectId(userId)
        val friendObjID = ObjectId(friendId)
        val user = userFriends.find(Filters.eq(User::_id.name, userObjID)).firstOrNull() ?: return false
        val friend = user.friends.find { it._id == friendObjID } ?: return false
        if (timestamp < friend.lastReadMessageTime) return false
        val newFriend = friend.copy(lastReadMessageTime = timestamp)
        val newFriends = user.friends - friend + newFriend
        return userFriends.updateOne(Filters.eq(User::_id.name, userObjID), Updates.set(User::friends.name, newFriends))
            .wasAcknowledged()
    }
}