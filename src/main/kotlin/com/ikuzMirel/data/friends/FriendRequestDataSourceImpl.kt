package com.ikuzMirel.data.friends

import com.mongodb.client.model.*
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId


class FriendRequestDataSourceImpl(
    db: MongoDatabase
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
        return friendRequests.find(
            Filters.eq(FriendRequest::_id.name, requestObjectId)
        ).firstOrNull() ?: throw Exception("Friend request not found")
    }

    override suspend fun getFriendRequestByUsers(senderId: String, receiverId: String): FriendRequest? {
        return friendRequests.find(
            Filters.and(
                Filters.eq(FriendRequest::senderId.name, senderId),
                Filters.eq(FriendRequest::receiverId.name, receiverId)
            )
        ).firstOrNull()
    }

    override suspend fun sendFriendRequest(friend: FriendRequest): Boolean {
        val findSendFR = friendRequests.find(
            Filters.and(
                Filters.eq(FriendRequest::senderId.name, friend.senderId),
                Filters.eq(FriendRequest::receiverId.name, friend.receiverId)
            )
        ).firstOrNull()
        val findReceiveFR = friendRequests.find(
            Filters.and(
                Filters.eq(FriendRequest::senderId.name, friend.receiverId),
                Filters.eq(FriendRequest::receiverId.name, friend.senderId)
            )
        ).firstOrNull()

        if (findSendFR != null && findReceiveFR != null) {
            return false
        }
        return friendRequests.insertOne(friend).wasAcknowledged()
    }

    override suspend fun cancelFriendRequest(requestId: String): Boolean {
        val requestObjectId = ObjectId(requestId)
        return friendRequests.deleteOne(Filters.eq(FriendRequest::_id.name, requestObjectId)).wasAcknowledged()
    }

    override suspend fun acceptFriendRequest(requestId: String): Boolean {
        val requestObjectId = ObjectId(requestId)
        return friendRequests.updateOne(
            Filters.eq(FriendRequest::_id.name, requestObjectId),
            Updates.set(FriendRequest::status.name, FriendRequestStatus.ACCEPTED.name)
        ).wasAcknowledged()
    }

    override suspend fun declineFriendRequest(requestId: String): Boolean {
        val requestObjectId = ObjectId(requestId)
        return friendRequests.updateOne(
            Filters.eq(FriendRequest::_id.name, requestObjectId),
            Updates.set(FriendRequest::status.name, FriendRequestStatus.REJECTED.name)
        ).wasAcknowledged()
    }

    private suspend fun getFriendRequestsWithAggregation(userId: String, type: Int): List<FriendRequest> {
        val matchType: Bson = when (type) {
            0 -> Filters.eq(FriendRequest::senderId.name, userId)
            else -> Filters.eq(FriendRequest::receiverId.name, userId)
        }
        val senderLet = listOf(Variable("senderId", "\$senderId"))
        val senderPipeline = listOf(
            Aggregates.match(
                Filters.expr(
//                    Filters.eq(User::_id.name, Document("\$toObjectId", FriendRequest::senderId.name))
                    Document("\$eq", listOf("\$_id", Document("\$toObjectId", "\$\$senderId")))
                )
            )
        )
        val receiverLet = listOf(Variable("receiverId", "\$receiverId"))
        val receiverPipeline = listOf(
            Aggregates.match(
                Filters.expr(
//                    Filters.eq(User::_id.name, Document("\$toObjectId", FriendRequest::senderId.name))
                    Document("\$eq", listOf("\$_id", Document("\$toObjectId", "\$\$receiverId")))
                )
            )
        )

        return friendRequests.aggregate<FriendRequest>(
            listOf(
                Aggregates.match(matchType),
                Aggregates.lookup(
                    "user",
                    senderLet,
                    senderPipeline,
                    FriendRequestWithAggregation::sender.name
                ),
                Aggregates.lookup(
                    "user",
                    receiverLet,
                    receiverPipeline,
                    FriendRequestWithAggregation::receiver.name
                ),
                Aggregates.project(
                    Projections.fields(
                        Projections.include(
                            FriendRequest::senderId.name,
                            FriendRequest::receiverId.name,
                            FriendRequest::status.name,
                            FriendRequest::_id.name
                        ),
                        Projections.computed(
                            FriendRequest::senderName.name,
                            Document("\$arrayElemAt", listOf("\$sender.username", 0))
                        ),
                        Projections.computed(
                            FriendRequest::receiverName.name,
                            Document("\$arrayElemAt", listOf("\$receiver.username", 0))
                        )
                    )
                )
            )
        ).toList()
    }
}