package com.ikuzMirel.data.friends

import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class FriendRequestDataSourceImpl(
    db: CoroutineDatabase
): FriendRequestDataSource {

    private val friendRequests = db.getCollection<FriendRequest>()
    override suspend fun getAllSentFriendRequests(userId: String): List<FriendRequest> {
        return friendRequests.find(FriendRequest::senderId eq userId).toList()
    }

    override suspend fun getAllReceivedFriendRequests(userId: String): List<FriendRequest> {
        return friendRequests.find(FriendRequest::receiverId eq userId).toList()
    }

    override suspend fun getFriendRequestById(requestId: String): FriendRequest {
        val requestObjectId = ObjectId(requestId)
        return friendRequests.findOne(FriendRequest::id eq requestObjectId)!!
    }

    override suspend fun sendFriendRequest(friend: FriendRequest): Boolean {
        val findSendFR = friendRequests.findOne(
            FriendRequest::senderId eq friend.senderId,
            FriendRequest::receiverId eq friend.receiverId
        )
        val findReceiveFR = friendRequests.findOne(
            FriendRequest::senderId eq friend.receiverId,
            FriendRequest::receiverId eq friend.senderId
        )
        if (findSendFR != null && findReceiveFR != null) {
            return false
        }
        return friendRequests.insertOne(friend).wasAcknowledged()
    }

    override suspend fun cancelFriendRequest(requestId: String): Boolean {
        val requestObjectId = ObjectId(requestId)
        return friendRequests.deleteOne(FriendRequest::id eq requestObjectId).wasAcknowledged()
    }

    override suspend fun acceptFriendRequest(requestId: String): Boolean {
        val requestObjectId = ObjectId(requestId)
        return friendRequests.updateOne(
            FriendRequest::id eq requestObjectId,
            setValue(FriendRequest::status, "Accepted")
        ).wasAcknowledged()
    }

    override suspend fun declineFriendRequest(requestId: String): Boolean {
        val requestObjectId = ObjectId(requestId)
        return friendRequests.updateOne(
            FriendRequest::id eq requestObjectId,
            setValue(FriendRequest::status, "Declined")
        ).wasAcknowledged()
    }
}