package com.ikuzMirel.data.friends

import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.eq
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.aggregate

class FriendRequestDataSourceImpl(
    db: CoroutineDatabase
) : FriendRequestDataSource {

    private val friendRequests = db.getCollection<FriendRequest>("friendRequest")

    override suspend fun getAllSentFriendRequests(userId: String): List<FriendRequest> {
        return getFriendRequestsWithAggregation(userId, 0)
    }

    override suspend fun getAllReceivedFriendRequests(userId: String): List<FriendRequest> {
        return getFriendRequestsWithAggregation(userId, 1)
    }

    override suspend fun getFriendRequestById(requestId: String): FriendRequest {
        val requestObjectId = ObjectId(requestId)
        return friendRequests.findOne(FriendRequest::id eq requestObjectId)!!
    }

    override suspend fun sendFriendRequest(friend: FriendRequest): Boolean {
        val findSendFR = friendRequests.findOne(
            FriendRequest::senderId eq friend.senderId, FriendRequest::receiverId eq friend.receiverId
        )
        val findReceiveFR = friendRequests.findOne(
            FriendRequest::senderId eq friend.receiverId, FriendRequest::receiverId eq friend.senderId
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
            FriendRequest::id eq requestObjectId, setValue(FriendRequest::status, FriendRequestStatus.ACCEPTED.name)
        ).wasAcknowledged()
    }

    override suspend fun declineFriendRequest(requestId: String): Boolean {
        val requestObjectId = ObjectId(requestId)
        return friendRequests.updateOne(
            FriendRequest::id eq requestObjectId, setValue(FriendRequest::status, FriendRequestStatus.REJECTED.name)
        ).wasAcknowledged()
    }

    private suspend fun getFriendRequestsWithAggregation(userId: String, type: Int): List<FriendRequest> {
        val matchType: Bson = when (type) {
            0 -> FriendRequest::senderId eq userId
            else -> FriendRequest::receiverId eq userId
        }
        return friendRequests.aggregate<FriendRequest>(
            match(matchType),
            lookup(
                from = "user",
                let = listOf(FriendRequest::senderId.variableDefinition()),
                resultProperty = FriendRequestWithAggregation::sender,
                pipeline = arrayOf(
                    match(
                        expr(
                            """{$eq: ["$ _id", { $ toObjectId: "$$ senderId"}] }""".trimIndent().formatJson().bson
                        )
                    )
                )
            ),
            lookup(
                from = "user",
                let = listOf(FriendRequest::receiverId.variableDefinition()),
                resultProperty = FriendRequestWithAggregation::receiver,
                pipeline = arrayOf(
                    match(
                        expr(
                            """{$eq: ["$ _id", { $ toObjectId: "$$ receiverId"}] }""".trimIndent().formatJson().bson
                        )
                    )
                )
            ),
            project(
                FriendRequest::senderId from FriendRequest::senderId,
                FriendRequest::receiverId from FriendRequest::receiverId,
                FriendRequest::status from FriendRequest::status,
                FriendRequest::id from FriendRequest::id,
                """{senderName: { $ arrayElemAt: ["$ sender.username", 0] }}""".trimIndent().formatJson().bson,
                """{receiverName: { $ arrayElemAt: ["$ receiver.username", 0] }}""".trimIndent().formatJson().bson
            )
        ).toList()
    }
}