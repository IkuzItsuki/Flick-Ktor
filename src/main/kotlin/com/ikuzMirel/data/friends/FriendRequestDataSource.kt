package com.ikuzMirel.data.friends

interface FriendRequestDataSource {

    suspend fun getAllSentFriendRequests(userId: String): List<FriendRequest>
    suspend fun getAllReceivedFriendRequests(userId: String): List<FriendRequest>
    suspend fun getFriendRequestById(requestId: String): FriendRequest?
    suspend fun getFriendRequestByUsers(senderId: String, receiverId: String): FriendRequest?
    suspend fun sendFriendRequest(friend: FriendRequest): Boolean
    suspend fun cancelFriendRequest(requestId: String): Boolean
    suspend fun acceptFriendRequest(requestId: String): Boolean
    suspend fun declineFriendRequest(requestId: String): Boolean
}