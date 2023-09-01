package com.ikuzMirel.data.friends

interface FriendDataSource {
    suspend fun getAllFriends(userId: String): List<Friend>
    suspend fun getFriendById(userId: String, friendId: String): Friend?
    suspend fun deleteFriend(userId: String, friend: Friend): Boolean
    suspend fun addFriend(userId: String, friend: Friend): Boolean
    suspend fun updateLastReadMessageTime(userId: String, friendId: String, timestamp: Long): Boolean
}